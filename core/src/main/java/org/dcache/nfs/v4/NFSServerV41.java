/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.nfs.v4;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.nfs4_prot_NFS4_PROGRAM_ServerStub;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.nfsstat;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.vfs.PseudoFs;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.commons.stats.RequestExecutionTimeGauges;
import org.dcache.nfs.status.BadSessionException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.status.ExpiredException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.MinorVersMismatchException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.NotOnlyOpException;
import org.dcache.nfs.status.OpIllegalException;
import org.dcache.nfs.status.OpNotInSessionException;
import org.dcache.nfs.status.ResourceException;
import org.dcache.nfs.status.RetryUncacheRepException;
import org.dcache.nfs.status.SequencePosException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.status.StaleClientidException;
import org.dcache.nfs.status.StaleStateidException;
import org.dcache.nfs.status.TooManyOpsException;
import org.dcache.nfs.v4.nlm.LockManager;
import org.dcache.nfs.v4.nlm.SimpleLm;

public class NFSServerV41 extends nfs4_prot_NFS4_PROGRAM_ServerStub {

    private static final Logger _log = LoggerFactory.getLogger(NFSServerV41.class);

    private static final RequestExecutionTimeGauges<String> GAUGES
            = new RequestExecutionTimeGauges<>(NFSServerV41.class.getName());

    private final VirtualFileSystem _fs;
    private final ExportFile _exportFile;
    private final NFSv4OperationFactory _operationFactory;
    private final NFSv41DeviceManager _deviceManager;
    private final NFSv4StateHandler _statHandler;
    private final LockManager _nlm;

    private NFSServerV41(Builder builder) {
        _deviceManager = builder.deviceManager;
        _fs = builder.vfs;
        _exportFile = builder.exportFile;
        _operationFactory = builder.operationFactory;
        _nlm = builder.nlm == null ? new SimpleLm() : builder.nlm;
        _statHandler = builder.stateHandler == null ? new NFSv4StateHandler() : builder.stateHandler;
    }

    @Deprecated
    public NFSServerV41(NFSv4OperationFactory operationFactory,
            NFSv41DeviceManager deviceManager, VirtualFileSystem fs,
            ExportFile exportFile) {

        _deviceManager = deviceManager;
        _fs = fs;
        _exportFile = exportFile;
        _operationFactory = operationFactory;
        _nlm = new SimpleLm();
        _statHandler = new NFSv4StateHandler();
    }

    @Override
    public void NFSPROC4_NULL_4(RpcCall call$) {
        _log.debug("NFS PING client: {}", call$.getTransport().getRemoteSocketAddress());
    }

    @Override
    public COMPOUND4res NFSPROC4_COMPOUND_4(RpcCall call$, COMPOUND4args arg1) {


        COMPOUND4res res = new COMPOUND4res();

        try {

            /*
             * here we have to checkfor utf8, but it's too much overhead to keep
             * spec happy.
             */
            res.tag = arg1.tag;
            String tag = arg1.tag.toString();
            MDC.put(NfsMdc.TAG, tag);
            MDC.put(NfsMdc.CLIENT, call$.getTransport().getRemoteSocketAddress().toString());

            _log.debug("NFS COMPOUND client: {}, tag: [{}]",
                    call$.getTransport().getRemoteSocketAddress(),
                    tag);

            int minorversion = arg1.minorversion.value;
            if ( minorversion > 1) {
                throw new MinorVersMismatchException(String.format("Unsupported minor version [%d]",arg1.minorversion.value) );
            }

	    if (arg1.argarray.length >= NFSv4Defaults.NFS4_MAX_OPS && minorversion == 0) {
		/*
		   in 4.1 maxops handled per session
		*/
		throw new ResourceException(String.format("Too many ops [%d]", arg1.argarray.length));
	    }
            res.resarray = new ArrayList<>(arg1.argarray.length);

            VirtualFileSystem fs = new PseudoFs(_fs, call$, _exportFile);

            CompoundContext context = new CompoundContextBuilder()
                    .withMinorversion(arg1.minorversion.value)
                    .withFs(fs)
                    .withDeviceManager(_deviceManager)
                    .withStateHandler(_statHandler)
                    .withLockManager(_nlm)
                    .withExportFile(_exportFile)
                    .withCall(call$)
                    .build();

            boolean retransmit = false;
            for (int position = 0; position <arg1.argarray.length; position++) {

                nfs_argop4 op = arg1.argarray[position];
                nfs_resop4 opResult = nfs_resop4.resopFor(op.argop);
                try {
                    if (minorversion != 0) {
                        checkOpPosition(op.argop, position, arg1.argarray.length);
                        if (position == 1) {
                            /*
                             * at this point we already have to have a session
                             */
			    if (arg1.argarray.length > context.getSession().getMaxOps()) {
				throw new TooManyOpsException(String.format("Too many ops [%d]", arg1.argarray.length));
			    }

                            List<nfs_resop4> cache = context.getCache();
                            if (cache != null) {

                                if (cache.isEmpty()) {
                                    /*
                                     * we got a duplicated request, but there
                                     * is nothing in the cache, though must be
                                     * as we are the second op in the compound.
                                     */
                                    throw new RetryUncacheRepException();
                                }

                                res.resarray.addAll(cache.subList(position, cache.size()));
                                res.status = statusOfLastOperation(cache);
                                retransmit = true;
                                break;
                            }
                        }
                    }
                    long t0 = System.nanoTime();
                    _operationFactory.getOperation(op).process(context, opResult);
                    GAUGES.update(nfs_opnum4.toString(op.argop), System.nanoTime() - t0);

                } catch (NfsIoException | ResourceException | ServerFaultException e) {
                    _log.error("NFS server fault: op: {} : {}", nfs_opnum4.toString(op.argop), e.getMessage());
                    opResult.setStatus(e.getStatus());
                } catch (BadXdrException | OpIllegalException | InvalException e) {
                    _log.warn("Faulty NFS client: op: {} : {}", nfs_opnum4.toString(op.argop), e.getMessage());
                    opResult.setStatus(e.getStatus());
                } catch (BadStateidException | StaleStateidException e) {
                    _log.warn("Bad Stateid: op: {} : {}", nfs_opnum4.toString(op.argop), e.getMessage());
                    opResult.setStatus(e.getStatus());
                } catch (ExpiredException | BadSessionException | StaleClientidException  e) {
                    _log.warn("Bad client: op: {} : {}", nfs_opnum4.toString(op.argop), e.getMessage());
                    opResult.setStatus(e.getStatus());
                } catch (ChimeraNFSException e) {
                    opResult.setStatus(e.getStatus());
                } catch (OncRpcException e) {
                    opResult.setStatus(nfsstat.NFSERR_BADXDR);
                    _log.warn("Bad xdr: {}: ", e.getMessage());
                }

                res.resarray.add(opResult);
                res.status = opResult.getStatus();
                if (res.status != nfsstat.NFS_OK) {
                    break;
                }
            }

            if (!retransmit && context.cacheThis()) {
                context.getSessionSlot().update(res.resarray);
            }

            _log.debug( "OP: [{}] status: {}", res.tag, res.status);

        } catch (ChimeraNFSException e) {
            _log.info("NFS operation failed: {}", e.getMessage());
            res.resarray = Collections.emptyList();
            res.status = e.getStatus();
        } catch (Exception e) {
            _log.error("Unhandled exception:", e);
            res.resarray = Collections.emptyList();
            res.status = nfsstat.NFSERR_SERVERFAULT;
        }finally{
            MDC.remove(NfsMdc.TAG);
            MDC.remove(NfsMdc.CLIENT);
            MDC.remove(NfsMdc.SESSION);
        }

        return res;
    }

    /**
     * Get {@link NFSv4StateHandler} used by this nfs server.
     * @return state handler.
     */
    public NFSv4StateHandler getStateHandler() {
        return _statHandler;
    }

    /*
     *
     * from NFSv4.1 spec:
     *
     * SEQUENCE MUST appear as the first operation of any COMPOUND in which
     * it appears.  The error NFS4ERR_SEQUENCE_POS will be returned when it
     * is found in any position in a COMPOUND beyond the first.  Operations
     * other than SEQUENCE, BIND_CONN_TO_SESSION, EXCHANGE_ID,
     * CREATE_SESSION, and DESTROY_SESSION, MUST NOT appear as the first
     * operation in a COMPOUND.  Such operations MUST yield the error
     * NFS4ERR_OP_NOT_IN_SESSION if they do appear at the start of a
     * COMPOUND.
     *
     */
    private static void checkOpPosition(int opCode, int position, int total) throws ChimeraNFSException {

        /*
         * special case of illegal operations.
         */
        if (opCode > nfs_opnum4.OP_RECLAIM_COMPLETE || opCode < nfs_opnum4.OP_ACCESS) {
            return;
        }

        if(position == 0 ) {
            switch(opCode) {
                case nfs_opnum4.OP_SEQUENCE:
                case nfs_opnum4.OP_CREATE_SESSION:
                case nfs_opnum4.OP_EXCHANGE_ID:
                case nfs_opnum4.OP_DESTROY_SESSION:
                case nfs_opnum4.OP_DESTROY_CLIENTID:
                    break;
                default:
                    throw new OpNotInSessionException();
            }

            if (total > 1) {
                switch (opCode) {
                    case nfs_opnum4.OP_CREATE_SESSION:
                    case nfs_opnum4.OP_DESTROY_CLIENTID:
                    case nfs_opnum4.OP_EXCHANGE_ID:
                        throw new NotOnlyOpException();
                    default:
                    // NOP
                }
            }

        } else {
            switch (opCode) {
                case nfs_opnum4.OP_SEQUENCE:
                    throw new SequencePosException();
            }
        }
    }

    private static int statusOfLastOperation(List<nfs_resop4> ops) {
        return ops.get(ops.size() -1).getStatus();
    }

    public RequestExecutionTimeGauges<String> getStatistics() {
        return GAUGES;
    }

    public static class Builder {

        private NFSv4OperationFactory operationFactory;
        private NFSv41DeviceManager deviceManager;
        private VirtualFileSystem vfs;
        private ExportFile exportFile;
        private LockManager nlm;
        private NFSv4StateHandler stateHandler;

        public Builder withDeviceManager(NFSv41DeviceManager deviceManager) {
            this.deviceManager = deviceManager;
            return this;
        }

        public Builder withOperationFactory(NFSv4OperationFactory operationFactory) {
            this.operationFactory = operationFactory;
            return this;
        }

        public Builder withVfs(VirtualFileSystem vfs) {
            this.vfs = vfs;
            return this;
        }

        public Builder withLockManager(LockManager nlm) {
            this.nlm = nlm;
            return this;
        }

        public Builder withExportFile(ExportFile exportFile) {
            this.exportFile = exportFile;
            return this;
        }

        public Builder withStateHandler(NFSv4StateHandler stateHandler) {
            this.stateHandler = stateHandler;
            return this;
        }

        public NFSServerV41 build() {
            return new NFSServerV41(this);
        }
    }
}
