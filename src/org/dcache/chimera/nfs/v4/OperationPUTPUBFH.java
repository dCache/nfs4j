package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTPUBFH4res;
import java.util.logging.Logger;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationPUTPUBFH extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationPUTPUBFH.class.getName());

	OperationPUTPUBFH(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_PUTPUBFH);
	}

	@Override
	public NFSv4OperationResult process() {
		_result.opputpubfh = new PUTPUBFH4res();
		_result.opputpubfh.status = nfsstat4.NFS4ERR_NOTSUPP;
    	return  new NFSv4OperationResult(_result, nfsstat4.NFS4ERR_NOTSUPP);
	}

}
