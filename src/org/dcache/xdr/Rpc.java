package org.dcache.xdr;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Rpc implements XdrDecodable, XdrEncodable {

	private final static Logger _log = Logger.getLogger(Rpc.class.getName());

	private int _xid;
	private int _type;

	private RpcCall _call;
	private RpcReply _reply;

	int xid() {
		return _xid;
	}

	int type() {
		return _type;
	}

	RpcCall call() {
		return _call;
	}	

	public void decode(Xdr xdr) throws XdrException {
		_xid = xdr.get_int();
		_type = xdr.get_int();
		_log.log(Level.FINEST, "type = " + _type);
		_log.log(Level.FINEST, "xid  = " + _xid);
		if(_type == RpcMessageType.CALL ) {
			_call = new RpcCall();
			xdr.decode(_call);
		}
	}

	public void encode(Xdr xdr) throws XdrException {
		xdr.put_int(_xid);
		xdr.put_int(_type);
		if(_type == RpcMessageType.REPLY ) {
			xdr.encode(_reply);
		}
	}

	void xid(int xid) {
		_xid = xid;
	}

}
