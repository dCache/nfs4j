package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.DELEGRETURN4res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationDELEGRETURN extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationDELEGRETURN.class);

    OperationDELEGRETURN(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_DELEGRETURN);
    }

    @Override
    public boolean process(CompoundContext context) {
        _result.opdelegreturn = new DELEGRETURN4res();
        _result.opdelegreturn.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }
}
