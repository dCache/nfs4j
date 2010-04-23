package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.WANT_DELEGATION4res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationWANT_DELEGATION extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationWANT_DELEGATION.class);

    public OperationWANT_DELEGATION(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_WANT_DELEGATION);
    }

    @Override
    public boolean process(CompoundContext context) {
        _result.opwant_delegation = new WANT_DELEGATION4res();
        _result.opwant_delegation.wdr_status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }
}
