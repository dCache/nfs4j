package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPEN_DOWNGRADE4res;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationOPEN_DOWNGRADE extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationOPEN_DOWNGRADE.class.getName());

	OperationOPEN_DOWNGRADE(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_OPEN_DOWNGRADE);
	}

	@Override
	public NFSv4OperationResult process() {
		_result.opopen_downgrade = new OPEN_DOWNGRADE4res();
		_result.opopen_downgrade.status = nfsstat4.NFS4ERR_NOTSUPP;
    	return  new NFSv4OperationResult(_result, nfsstat4.NFS4ERR_NOTSUPP);
	}

}
