/*
 * Copyright (c) 2021 - 2025 Deutsches Elektronen-Synchroton,
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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.OpenModeException;
import org.dcache.nfs.v4.xdr.COPY4res;
import org.dcache.nfs.v4.xdr.COPY4resok;
import org.dcache.nfs.v4.xdr.copy_requirements4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stable_how4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.xdr.write_response4;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
/**
 * NFSv4.2 operations that handles server side copy as specified in rfc7862#section-4.
 */
public class OperationCOPY extends AbstractNFSv4Operation {

    private final static Logger LOGGER = LoggerFactory.getLogger(OperationCOPY.class);

    public OperationCOPY(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_COPY);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        final COPY4res res =  result.opcopy;

        // inter server copy is not supported
        if (_args.opcopy.ca_source_server.length > 0) {
            throw new NotSuppException("Inter-server copy is not supported");
        }

        // only consecutive copy is supported
        if (!_args.opcopy.ca_consecutive) {
            res.cr_requirements = new copy_requirements4();
            res.cr_requirements.cr_consecutive = true;
            res.cr_requirements.cr_synchronous = _args.opcopy.ca_synchronous;
            res.cr_status = nfsstat.NFS4ERR_OFFLOAD_NO_REQS;
            return;
        }

        Inode srcInode = context.savedInode();
        Inode dstInode = context.currentInode();

        long srcPos = _args.opcopy.ca_src_offset.value;
        long dstPos = _args.opcopy.ca_dst_offset.value;
        long len = _args.opcopy.ca_count.value;

        NFS4Client client = context.getSession().getClient();

        NFS4State srcState = client.state(_args.opcopy.ca_src_stateid);
        NFS4State dstState = client.state(_args.opcopy.ca_dst_stateid);

        int srcAccess = context.getStateHandler().getFileTracker()
              .getShareAccess(client, srcInode, srcState.getOpenState().stateid());

        int dstAccess = context.getStateHandler().getFileTracker()
              .getShareAccess(client, dstInode, dstState.getOpenState().stateid());

        if ((srcAccess & nfs4_prot.OPEN4_SHARE_ACCESS_READ) == 0) {
            throw new OpenModeException("Invalid source inode open mode (required read)");
        }

        if ((dstAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) == 0) {
            throw new OpenModeException("Invalid destination inode open mode (required write)");
        }

        res.cr_resok4 = new COPY4resok();
        res.cr_resok4.cr_response = new write_response4();
        res.cr_resok4.cr_response.wr_writeverf = context.getRebootVerifier();
        res.cr_resok4.cr_response.wr_callback_id = new stateid4[]{};
        res.cr_resok4.cr_response.wr_committed = stable_how4.FILE_SYNC4;
        res.cr_resok4.cr_response.wr_count = new length4(0);

        res.cr_resok4.cr_requirements = new copy_requirements4();
        res.cr_resok4.cr_requirements.cr_consecutive = true;
        res.cr_status = nfsstat.NFS_OK;

        CompletableFuture<Long> copyFuture = context.getFs().copyFileRange(srcInode, srcPos, dstInode, dstPos, len);
        boolean isSync = _args.opcopy.ca_synchronous;
        if (isSync) {
            try {
                // try sync copy and fall-back to async
                long n = copyFuture.get(1, TimeUnit.SECONDS);
                res.cr_resok4.cr_response.wr_count = new length4(n);
            } catch (InterruptedException | ExecutionException e) {

                Throwable cause = Throwables.getRootCause(e);
                Throwables.propagateIfPossible(cause, ChimeraNFSException.class);

                LOGGER.error("Copy-offload failed: {}", e.getMessage());
                res.cr_status = nfsstat.NFSERR_IO;
            } catch (TimeoutException e) {
                // continue as async copy
                isSync = false;
            }
        }

        if (!isSync) {
            // copy asynchronously
            var copyState = notifyWhenComplete(client, dstInode, context.getRebootVerifier(), copyFuture);
            res.cr_resok4.cr_response.wr_callback_id = new stateid4[]{copyState};
        }
        res.cr_resok4.cr_requirements.cr_synchronous = isSync;
    }

    private stateid4 notifyWhenComplete(NFS4Client client, Inode dstInode, verifier4 verifier, CompletableFuture<Long> copyFuture) throws ChimeraNFSException {
        var openState = client.state(_args.opcopy.ca_src_stateid);
        var copyState = client.createServerSideCopyState(openState.getStateOwner(), openState).stateid();

        copyFuture.handle((n, t) -> {

            var cr_response = new write_response4();
            cr_response.wr_callback_id = new stateid4[]{};
            cr_response.wr_committed = stable_how4.FILE_SYNC4;
            cr_response.wr_count = new length4(n);
            cr_response.wr_writeverf = verifier;

            try {
                client.getCB().cbOffload(new nfs_fh4(dstInode.toNfsHandle()), copyState, cr_response, toNfsState(t));
            } catch (IOException ex) {
                LOGGER.warn("Failed to notify client about copy-offload completion: {}", ex.getMessage());
            }

            return null;
        });

        return copyState;
    }

    private int toNfsState(Throwable t) {

        if (t == null) {
            return nfsstat.NFS_OK;
        }

        // FIXME: we need some mapping between 'well known' exceptions and nfs error states.
        LOGGER.warn("Copy-offload failed with exception: {}", Throwables.getRootCause(t).toString());
        return nfsstat.NFSERR_IO;
    }
}
