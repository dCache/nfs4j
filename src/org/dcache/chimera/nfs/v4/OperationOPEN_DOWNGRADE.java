package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPEN_DOWNGRADE4res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOPEN_DOWNGRADE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationOPEN_DOWNGRADE.class);

    OperationOPEN_DOWNGRADE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN_DOWNGRADE);
    }

    @Override
    public boolean process(CompoundContext context) {
        _result.opopen_downgrade = new OPEN_DOWNGRADE4res();
        _result.opopen_downgrade.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }
}
