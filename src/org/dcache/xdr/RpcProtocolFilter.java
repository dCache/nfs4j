package org.dcache.xdr;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.ProtocolParser;
import com.sun.grizzly.util.OutputWriter;
import java.io.IOException;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;
import java.util.logging.Level;

class RpcProtocolFilter implements ProtocolFilter {

    public static final String RPC_CALL = "RPC_CALL";
    public static final String RPC_CALL_INFO = "RPC_CALL_INFO";
    public static final String RPC_XDR = "RPC_XDR";
    
    private final static Logger _log = Logger.getLogger(RpcProtocolFilter.class.getName());

    @Override
    public boolean execute(Context context) throws IOException {
        Xdr  xdr = (Xdr) context.removeAttribute(ProtocolParser.MESSAGE);
        
        if (xdr == null) {
            return false;
        }
        
        xdr.startDecode();

        int xid = xdr.get_int();
        int type = xdr.get_int();
        RpcMsg msg;
        if (type == RpcMessageType.CALL) {
            msg = new RpcCall(xid);
            try {
               msg.xdrDecode(xdr); 
            }catch (XdrException e) {
                reply(e, xid, context);
            }
        } else {
            /*
             * For now I do not expect to receive a reply message over
             * the client connection. But it's definitely part of
             * bidirectional RPC calls. 
             */            
            msg = new RpcReply(xid);
            _log.log(Level.INFO, "Reply received: " + msg);
        }

        SocketChannel socketChannel = ((SocketChannel)context.getSelectionKey().channel());
        InetSocketAddress local =(InetSocketAddress) socketChannel.socket().getLocalSocketAddress();
        InetSocketAddress remote =(InetSocketAddress)socketChannel.socket().getRemoteSocketAddress();
        RpcCallInfo callInfo = new RpcCallInfo(local, remote);
        
        /*
         * pass RPC call and info to the next filter in the chain
         */
        
        context.setAttribute(RPC_CALL, msg);
        context.setAttribute(RPC_CALL_INFO, callInfo);        
        context.setAttribute(RPC_XDR, xdr);
        
        _log.log(Level.FINE, "New message to process: " + msg);

        return true;
    }

    @Override
    public boolean postExecute(Context context) throws IOException {
        
        /**
         * cleanup
         */
        context.removeAttribute(RPC_CALL);
        context.removeAttribute(RPC_CALL_INFO);
        context.removeAttribute(RPC_XDR);
        
        return true;
    }

    private void reply(XdrException re, int xid, Context context) throws IOException {

        Xdr xdr = new Xdr(1024);

        try {
            RpcReply msg = new RpcReply(xid);
            msg.setMessageObject(re);
            
            xdr.startEncode();
            xdr.encode(msg);
            xdr.stopEncode();
        }catch(XdrException e ) {
            throw new IOException(e.getMessage());
        }

        SelectableChannel channel = context.getSelectionKey().channel();
        ByteBuffer message = xdr.body();

        OutputWriter.flushChannel(channel, message);

    }

}
