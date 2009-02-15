package org.dcache.xdr;

import java.net.InetSocketAddress;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RpcCallInfo {

	private final static Logger _log = Logger.getLogger(RpcCallInfo.class.getName());
	
	private final InetSocketAddress _remote;
	private final InetSocketAddress _local;

	RpcCallInfo() {
		this(null, null);
	}

	RpcCallInfo(InetSocketAddress local, InetSocketAddress remote) {
		_local = local;
		_remote = remote;
		_log.log(Level.FINEST, "Rpc call: remote/local: " + _remote + "/" + _local);
	}

	InetSocketAddress getLocalSocketAddress() {
		return _local;
	}

	InetSocketAddress getRemoteSocketAddress() {
		return _remote;
	}
}
