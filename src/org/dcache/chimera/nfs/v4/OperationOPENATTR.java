package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPENATTR4res;
import org.apache.log4j.Logger;

public class OperationOPENATTR extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationOPENATTR.class.getName());

	OperationOPENATTR(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_OPENATTR);
	}

    @Override
    public boolean process(CompoundContext context) {
        _result.opopenattr = new OPENATTR4res();
        _result.opopenattr.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }

}
