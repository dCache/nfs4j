package org.dcache.xdr;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.ProtocolParser;
import com.sun.grizzly.util.OutputWriter;
import java.io.IOException;


import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.logging.Logger;
import java.util.logging.Level;

class RpcProtocolFilter implements ProtocolFilter {

	private final static Logger _log = Logger.getLogger(RpcProtocolFilter.class.getName());

	public RpcProtocolFilter() {
	}

	@Override
	public boolean execute(Context context) throws IOException {
		Rpc  rpc = (Rpc) context.removeAttribute(ProtocolParser.MESSAGE);		
		
        if (rpc == null) {
            return false;
        }

		try {
			RpcMsg rpcMsg =  rpc.getMessage();
			RpcCallInfo _callInfo = new RpcCallInfo();
			_log.log(Level.FINE, "New message to process: " + rpcMsg);
		}catch(RpcException re) {
			_log.log(Level.INFO, "RPC exception: " + re.getMessage());
			reply(re, rpc, context);
			return false;
		}
		return true;
	}

	@Override
	public boolean postExecute(Context arg0) throws IOException {
		return true;
	}

	private void reply(RpcException re, Rpc rpc, Context context) throws IOException {

		ByteBuffer buf = ByteBuffer.allocate(1024);
		RpcReply reply = new RpcReply(rpc.xid(), buf);

		re.encode(reply);

		SelectableChannel channel = context.getSelectionKey().channel();
		ByteBuffer message = reply.body();
		int len = message.position() -4 ;
		message.putInt(0, len |= 0x80000000 );
		message.flip();
		OutputWriter.flushChannel(channel, message);

	}

}
