package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.COMMIT4res;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationCOMMIT extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationCOMMIT.class.getName());

	public OperationCOMMIT(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_COMMIT);
	}

	@Override
	public NFSv4OperationResult process() {
    	_result.opcommit = new COMMIT4res();
    	return  new NFSv4OperationResult(_result, nfsstat4.NFS4ERR_NOTSUPP);
	}

}
