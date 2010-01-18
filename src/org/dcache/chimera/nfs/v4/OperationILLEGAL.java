package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.ILLEGAL4res;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationILLEGAL extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationILLEGAL.class.getName());

	public OperationILLEGAL(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 op, ExportFile exports) {
		super(fs, exports, call$, fh, op, nfs_opnum4.OP_ILLEGAL);
	}

	@Override
	public NFSv4OperationResult process() {

		_log.info("ILLEGAL OPCODE:" + _args.argop);

		ILLEGAL4res opResILLEGAL = new ILLEGAL4res();
		opResILLEGAL.status = nfsstat4.NFS4ERR_OP_ILLEGAL;
		_result.opillegal = opResILLEGAL;
		return new NFSv4OperationResult(_result, nfsstat4.NFS4ERR_OP_ILLEGAL);
	}

}
