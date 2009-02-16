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
        Xdr  xdr = (Xdr) context.removeAttribute(ProtocolParser.MESSAGE);

        if (xdr == null) {
            return false;
        }

        Rpc rpc =  new Rpc();
        try {

            xdr.decode(rpc);
            RpcCall call = rpc.call();
            RpcCallInfo _callInfo = new RpcCallInfo();
            _log.log(Level.FINE, "New message to process: " + call);

        }catch(XdrException re) {
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

    private void reply(XdrException re, Rpc rpc, Context context) throws IOException {

        Xdr xdr = new Xdr(1024);

        try {
            xdr.startEncode();
            xdr.put_int(rpc.xid());
            xdr.put_int(RpcMessageType.REPLY);
            re.xdrEncode(xdr);
            xdr.stopEncode();
        }catch(XdrException e ) {
            throw new IOException(e.getMessage());
        }

        SelectableChannel channel = context.getSelectionKey().channel();
        ByteBuffer message = xdr.body();

        OutputWriter.flushChannel(channel, message);


    }

}
