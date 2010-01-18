/*
 * $Id:NFSServerV41.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot_NFS4_PROGRAM_ServerStub;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4res;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4args;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcCall;

public class NFSServerV41 extends nfs4_prot_NFS4_PROGRAM_ServerStub {

    private final FileSystemProvider _fs;
    private final ExportFile _exportFile;
    private static final Logger _log = Logger.getLogger(NFSServerV41.class.getName());

    public NFSServerV41(NFSv41DeviceManager deviceManager, FileSystemProvider fs, ExportFile exportFile) throws OncRpcException, IOException {

        NFSv41DeviceManagerFactory.setDeviceManager(deviceManager);
        _fs = fs;
        _exportFile = exportFile;
    }

    @Override
    public void NFSPROC4_NULL_4(RpcCall call$) {
    }

    @Override
    public COMPOUND4res NFSPROC4_COMPOUND_4(RpcCall call$, COMPOUND4args arg1) {


        COMPOUND4res res = new COMPOUND4res();

        try {

            _log.log(Level.FINE, "NFS COMPOUND client: {0}, tag: {1}",
                    new Object[]{call$.getTransport().getRemoteSocketAddress(),
                        new String(arg1.tag.value.value)});

            List<nfs_resop4> v = new ArrayList<nfs_resop4>(arg1.argarray.length);
            if (arg1.minorversion.value > 1) {
                res.status = nfsstat4.NFS4ERR_MINOR_VERS_MISMATCH;
                res.resarray = new nfs_resop4[0];
                _log.log(Level.FINE, "      : NFS4ERR_MINOR_VERS_MISMATCH");
            } else {

                CompoundContext context = new CompoundContext(v, arg1.minorversion.value,
                        _fs, call$, _exportFile);

                for (nfs_argop4 op:  arg1.argarray) {

                    if( ! NFSv4OperationFactory.getOperation(op).process(context) ) {
                        break;
                    }
                }

                try {
                    _log.log(Level.FINE, "CURFH: {0}", context.currentInode().toFullString());
                } catch (ChimeraNFSException he) {
                    _log.fine("CURFH: NULL");
                }

                v = context.processedOperations();
                res.resarray = v.toArray(new nfs_resop4[v.size()]);
                // result  status must be equivalent
                // to the status of the last operation that
                // was executed within the COMPOUND procedure
                if(!v.isEmpty()) {
                    res.status = res.resarray[res.resarray.length - 1].getStatus();
                }else{
                    res.status = nfsstat4.NFS4_OK;
                }
            }

            res.tag = arg1.tag;
            _log.log(Level.FINE, "OP: {1} status: {1}", new Object[]{res.tag, res.status});

        } catch (Exception e) {
            _log.log(Level.SEVERE, "Unhandled exception:", e);
            res.resarray = new nfs_resop4[0];
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
            res.tag = arg1.tag;
        }

        return res;
    }
}
