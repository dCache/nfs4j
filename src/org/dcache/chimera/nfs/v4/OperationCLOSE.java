package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.CLOSE4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.FsInode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationCLOSE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationCLOSE.class);

    OperationCLOSE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_CLOSE);
    }

    @Override
    public boolean process(CompoundContext context) {
        CLOSE4res res = new CLOSE4res();

        try {

            FsInode inode = context.currentInode();

            if( context.getSession() == null ) {
                context.getStateHandler().updateClientLeaseTime(_args.opclose.open_stateid);
            }else{
                context.getSession().getClient().updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);
            }

            res.status = nfsstat4.NFS4_OK;

            res.open_stateid = new stateid4();
            res.open_stateid.seqid = _args.opclose.open_stateid.seqid;
            res.open_stateid.other = _args.opclose.open_stateid.other;

        }catch(ChimeraNFSException he) {
            _log.debug("CLOSE: {}", he.getMessage() );
            res.status = he.getStatus();
        }

        _result.opclose = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;
    }

}
