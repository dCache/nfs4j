package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPEN_DOWNGRADE4res;
import org.apache.log4j.Logger;

public class OperationOPEN_DOWNGRADE extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationOPEN_DOWNGRADE.class.getName());

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
