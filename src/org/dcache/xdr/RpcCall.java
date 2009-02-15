package org.dcache.xdr;

import java.nio.ByteBuffer;

public class RpcCall extends RpcMsg {

	private int _prog;
	private int _version;
	private int _proc;
	private int _rpcvers;

	private final Xdr _xdr;

	private final RpcAuth _auth;


	public RpcCall(int xid, ByteBuffer body) throws RpcException {
		super(xid, RpcMessageType.CALL, body) ;
		_xdr = super.xdr();
		_rpcvers = _xdr.get_int();
		_prog = _xdr.get_int();
		_version = _xdr.get_int();
		if( _version != 5) {
			throw new RpcMismatchException("RPC version mismatch: " + _version, 2, 2);
		}
		_proc = _xdr.get_int();

		_auth = RpcAuthDecoder.getRpcAuth(_xdr);
		

	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(_auth);

		sb.append("RPC ver  : ").append(_rpcvers).append("\n");
		sb.append("Program  : ").append(_prog).append("\n");
		sb.append("Version  : ").append(_version).append("\n");
		sb.append("Procedure: ").append(_proc).append("\n");
		
		return sb.toString();
	}

}
