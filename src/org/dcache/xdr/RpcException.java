package org.dcache.xdr;

public class RpcException extends Exception {
	
	private static final long serialVersionUID = 5492424750571897543L;

	private final int _replyStatus;

	RpcException(int replyStatus, String msg) {
		super(msg);
		_replyStatus = replyStatus;
	}


	void encode(RpcReply reply) {

		Xdr xdr = reply.xdr();
		xdr.put_int(reply.xid());
		xdr.put_int(reply.type());
		xdr.put_int(_replyStatus);

	}

}
