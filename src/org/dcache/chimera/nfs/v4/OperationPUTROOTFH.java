package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTROOTFH4res;
import org.dcache.chimera.FsInode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationPUTROOTFH extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationPUTROOTFH.class);

    public OperationPUTROOTFH(nfs_argop4 op) {
        super(op, nfs_opnum4.OP_PUTROOTFH);
    }

    @Override
    public boolean process(CompoundContext context) {

        PUTROOTFH4res res = new PUTROOTFH4res();

        try {
            context.currentInode(FsInode.getRoot(context.getFs()));
            res.status = nfsstat4.NFS4_OK;
        } catch (Exception e) {
            _log.error("PUTROOTFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opputrootfh = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;

    }
}
