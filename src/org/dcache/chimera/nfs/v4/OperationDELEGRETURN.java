package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.DELEGRETURN4res;
import org.apache.log4j.Logger;

public class OperationDELEGRETURN extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationDELEGRETURN.class.getName());

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
