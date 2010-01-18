package org.dcache.chimera.nfs.v4;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.CLOSE4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;

public class OperationCLOSE extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationCLOSE.class.getName());


    /*
     * reflects nfsv4.1
     */
    private final boolean _isPNFS = false;

    OperationCLOSE(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
        super(fs, exports, call$, fh, args, nfs_opnum4.OP_CLOSE);
    }

    @Override
    public NFSv4OperationResult process() {
        CLOSE4res res = new CLOSE4res();

        try {

            FsInode inode = _fh.currentInode();

            if( _fh.getSession() == null ) {
                NFSv4StateHandler.getInstace().updateClientLeaseTime(_args.opclose.open_stateid);
            }else{
                _fh.getSession().getClient().updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);
            }

            res.status = nfsstat4.NFS4_OK;

            res.open_stateid = new stateid4();
            res.open_stateid.seqid = _args.opclose.open_stateid.seqid;
            res.open_stateid.other = _args.opclose.open_stateid.other;

        }catch(ChimeraNFSException he) {
            _log.log(Level.FINER, "CLOSE: {0}", he.getMessage() );
            res.status = he.getStatus();
        }

        _result.opclose = res;

        return new NFSv4OperationResult(_result, res.status);
    }

}
