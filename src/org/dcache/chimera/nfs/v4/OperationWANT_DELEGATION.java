package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.WANT_DELEGATION4res;
import org.apache.log4j.Logger;

public class OperationWANT_DELEGATION extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationWANT_DELEGATION.class.getName());

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
