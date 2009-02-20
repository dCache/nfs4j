package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.ProtocolParser;

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

        xdr.beginDecoding();

        int xid = xdr.xdrDecodeInt();
        int type = xdr.xdrDecodeInt();

        if (type == RpcMessageType.CALL) {
            RpcCall call = new RpcCall(xid, context);
            try {
               call.xdrDecode(xdr);

               SocketChannel socketChannel = ((SocketChannel)context.getSelectionKey().channel());
               InetSocketAddress local =(InetSocketAddress) socketChannel.socket().getLocalSocketAddress();
               InetSocketAddress remote =(InetSocketAddress)socketChannel.socket().getRemoteSocketAddress();
               RpcCallInfo callInfo = new RpcCallInfo(local, remote);

               /*
                * pass RPC call and info to the next filter in the chain
                */

               context.setAttribute(RPC_CALL, call);
               context.setAttribute(RPC_CALL_INFO, callInfo);
               context.setAttribute(RPC_XDR, xdr);

               _log.log(Level.FINE, "New message to process: " + call);

            }catch (RpcException e) {
                call.reject( e.getRpcReply() );
                _log.log(Level.INFO, "RPC request rejected: " + e.getMessage());
                return false;
            }catch (OncRpcException e) {
                _log.log(Level.INFO, "failed to process RPC request: " + e.getMessage());
                return false;
            }
        } else {
            /*
             * For now I do not expect to receive a reply message over
             * the client connection. But it's definitely part of
             * bidirectional RPC calls.
             */
            RpcReply msg = new RpcReply();
            _log.log(Level.INFO, "Reply received: " + msg);
        }

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

}
