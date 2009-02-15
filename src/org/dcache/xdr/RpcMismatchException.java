package org.dcache.xdr;

public class RpcMismatchException extends RpcDenyException {
	private static final long serialVersionUID = 119916123488742510L;
	private int _min;
	private int _max;

	RpcMismatchException(String msg, int min, int max) {
		super(RpcRejectStatus.RPC_MISMATCH, msg);
		_min = min;
		_max = max;
	}

	@Override
	void encode(RpcReply reply) {
		super.encode(reply);
		Xdr xdr = reply.xdr();
		xdr.put_int(_min);
		xdr.put_int(_max);
	}
}
