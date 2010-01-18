package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOCK4res;
import org.apache.log4j.Logger;

public class OperationLOCK extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLOCK.class.getName());

	OperationLOCK(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_LOCK);
	}

    @Override
    public boolean process(CompoundContext context) {
        _result.oplock = new LOCK4res();
        _result.oplock.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }

}
