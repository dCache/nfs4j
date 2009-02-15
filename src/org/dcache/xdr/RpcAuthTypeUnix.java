package org.dcache.xdr;

import java.util.logging.Logger;
import java.util.Arrays;

public class RpcAuthTypeUnix implements RpcAuth {

	private final RpcAuthType _type =  RpcAuthType.UNIX;

	private final int _uid;
	private final int _gid;
	private final int _gids[];
	private final int _stamp;
	private final String _machine;

	private final static Logger _log = Logger.getLogger(RpcAuthTypeUnix.class.getName());

	RpcAuthTypeUnix(Xdr xdr) {

		int len = xdr.get_int();
		_stamp = xdr.get_int();
		_machine = xdr.get_string();
		_uid = xdr.get_int();
		_gid = xdr.get_int();
		_gids = xdr.get_int_array();
	}

	public RpcAuthType type() {
		return _type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Host: ").append(_machine).append("\n");
		sb.append("timestamp: ").append(_stamp).append("\n");
		sb.append("uid: ").append(_uid).append("\n");
		sb.append("gid: ").append(_gid).append("\n");
		sb.append("gids: ").append(Arrays.toString(_gids)).append("\n");
		
		return sb.toString();
	}

}
