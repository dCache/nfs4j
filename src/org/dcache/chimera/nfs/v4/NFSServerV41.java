/*
 * $Id:NFSServerV41.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4args;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4res;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot_NFS4_PROGRAM_ServerStub;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NFSServerV41 extends nfs4_prot_NFS4_PROGRAM_ServerStub {

    private final FileSystemProvider _fs;
    private final ExportFile _exportFile;
    private static final Logger _log = LoggerFactory.getLogger(NFSServerV41.class);
    private final NFSv4OperationFactory _operationFactory;
    private final NFSv41DeviceManager _deviceManager;
    private final AclHandler _aclHandler;

    public NFSServerV41(NFSv4OperationFactory operationFactory,
            NFSv41DeviceManager deviceManager, AclHandler aclHandler, FileSystemProvider fs,
            ExportFile exportFile) throws OncRpcException, IOException {

        _deviceManager = deviceManager;
        _fs = fs;
        _exportFile = exportFile;
        _operationFactory = operationFactory;
        _aclHandler = aclHandler;
    }

    @Override
    public void NFSPROC4_NULL_4(RpcCall call$) {
        _log.debug("NFS PING client: {}", call$.getTransport().getRemoteSocketAddress());
    }

    @Override
    public COMPOUND4res NFSPROC4_COMPOUND_4(RpcCall call$, COMPOUND4args arg1) {


        COMPOUND4res res = new COMPOUND4res();

        try {

            _log.debug("NFS COMPOUND client: {}, tag: [{}]",
                    call$.getTransport().getRemoteSocketAddress(),
                    new String(arg1.tag.value.value));

            MDC.put(NfsMdc.TAG, new String(arg1.tag.value.value) );
            MDC.put(NfsMdc.CLIENT, call$.getTransport().getRemoteSocketAddress().getHostName());

            List<nfs_resop4> v = new ArrayList<nfs_resop4>(arg1.argarray.length);
            if (arg1.minorversion.value > 1) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_MINOR_VERS_MISMATCH,
                    String.format("Unsupported minor version [%d]",arg1.minorversion.value) );
            }

            CompoundContext context = new CompoundContext(v, arg1.minorversion.value,
                _fs, _deviceManager, _aclHandler, call$, _exportFile);

            for (nfs_argop4 op : arg1.argarray) {

                if (!_operationFactory.getOperation(op).process(context)) {
                    break;
                }
            }

            try {
               _log.debug("CURFH: {}", context.currentInode().toFullString());
            } catch (ChimeraNFSException he) {
                _log.debug("CURFH: NULL");
            }

            v = context.processedOperations();
            res.resarray = v.toArray(new nfs_resop4[v.size()]);
            // result  status must be equivalent
            // to the status of the last operation that
            // was executed within the COMPOUND procedure
            if (!v.isEmpty()) {
                res.status = res.resarray[res.resarray.length - 1].getStatus();
            } else {
                res.status = nfsstat4.NFS4_OK;
            }

            res.tag = arg1.tag;
            _log.debug( "OP: [{}] status: {}", res.tag, res.status);

        } catch (ChimeraNFSException e) {
            _log.info("NFS operation failed: {}", e.getMessage());
            res.resarray = new nfs_resop4[0];
            res.status = e.getStatus();
            res.tag = arg1.tag;
        } catch (Exception e) {
            _log.error("Unhandled exception:", e);
            res.resarray = new nfs_resop4[0];
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
            res.tag = arg1.tag;
        }finally{
            MDC.remove(NfsMdc.TAG);
            MDC.remove(NfsMdc.CLIENT);
            MDC.remove(NfsMdc.SESSION);
        }

        return res;
    }
}
