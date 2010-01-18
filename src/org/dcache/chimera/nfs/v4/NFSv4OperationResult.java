package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;

public class NFSv4OperationResult {

	final nfs_resop4 _opRes;
	final int _status;

	public NFSv4OperationResult(nfs_resop4 res, int status) {
		_opRes = res;
		_status = status;
	}

	public int getStatus() {
		return _status;
	}

	public nfs_resop4 getResult() {
		return _opRes;
	}

}
