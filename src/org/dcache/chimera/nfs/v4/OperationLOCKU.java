package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOCKU4res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLOCKU extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationLOCKU.class);

    OperationLOCKU(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_LOCKU);
    }

    @Override
    public boolean process(CompoundContext context) {
        _result.oplocku = new LOCKU4res();
        _result.oplocku.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }
}
