package org.dcache.xdr;


public class RpcDenyException  extends XdrException {
	
	private static final long serialVersionUID = -83246023219821408L;

	private final int _rejectStatus;

	RpcDenyException(int status, String msg) {
		super(RpcReplyStats.MSG_DENIED, msg);
		_rejectStatus = status;
	}

	@Override
	public void encode(Xdr xdr) {
		super.encode(xdr);
		xdr.put_int(_rejectStatus);
	}

}
