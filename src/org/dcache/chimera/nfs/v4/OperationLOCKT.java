package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOCKT4res;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationLOCKT extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLOCKT.class.getName());

	OperationLOCKT(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_LOCKT);
	}

	@Override
	public NFSv4OperationResult process() {
		_result.oplockt = new LOCKT4res();
		_result.oplockt.status = nfsstat4.NFS4ERR_NOTSUPP;
    	return  new NFSv4OperationResult(_result, nfsstat4.NFS4ERR_NOTSUPP);
	}

}
