package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.WANT_DELEGATION4res;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationWANT_DELEGATION extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationWANT_DELEGATION.class.getName());

	public OperationWANT_DELEGATION(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_WANT_DELEGATION);
	}

	@Override
	public NFSv4OperationResult process() {
		_result.opwant_delegation = new WANT_DELEGATION4res();
		_result.opwant_delegation.wdr_status = nfsstat4.NFS4ERR_NOTSUPP;
    	return  new NFSv4OperationResult(_result, nfsstat4.NFS4ERR_NOTSUPP);
	}

}
