package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTFH4res;
import org.dcache.chimera.nfs.NFSHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationPUTFH extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationPUTFH.class);

    public OperationPUTFH(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_PUTFH);
    }

    @Override
    public boolean process(CompoundContext context) {
        PUTFH4res res = new PUTFH4res();

        try {
            context.currentInode(NFSHandle.toFsInode(context.getFs(), _args.opputfh.object.value));
            _log.debug("NFS Request  PUTFH4 current: {}", context.currentInode().toFullString());
            res.status = nfsstat4.NFS4_OK;
        } catch (IllegalArgumentException iae) {
            res.status = nfsstat4.NFS4ERR_BADHANDLE;
        } catch (Exception e) {
            _log.error("PUTFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opputfh = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;
    }
}
