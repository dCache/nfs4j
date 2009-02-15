package org.dcache.xdr;


public class RpcDenyException  extends RpcException {
	
	private static final long serialVersionUID = -83246023219821408L;

	private final int _rejectStatus;

	RpcDenyException(int status, String msg) {
		super(RpcReplyStats.MSG_DENIED, msg);
		_rejectStatus = status;
	}

	@Override
	void encode(RpcReply reply) {
		super.encode(reply);
		Xdr xdr = reply.xdr();
		xdr.put_int(_rejectStatus);
	}

}
