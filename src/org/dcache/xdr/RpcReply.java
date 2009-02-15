package org.dcache.xdr;

import java.nio.ByteBuffer;

public class RpcReply extends RpcMsg {

	RpcReply(int xid, ByteBuffer body) {
		super(xid, RpcMessageType.REPLY, body);
		// reserve a space for message lenght
		body.position(4);
	}

}
