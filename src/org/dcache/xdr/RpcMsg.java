package org.dcache.xdr;

import java.nio.ByteBuffer;

public class RpcMsg {

	private final int _xid;
	private final int _type;
	private final ByteBuffer _body;

	RpcMsg(int xid, int type, ByteBuffer body ){
		_xid = xid;
		_type = type;
		_body = body;
	}

	int xid() {
		return _xid;
	}

	int type() {
		return _type;
	}
	
	public Xdr xdr() {
		return new Xdr(_body);
	}

	public ByteBuffer body() {
		return _body;
	}

}
