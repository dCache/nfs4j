/*
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.dcache.xdr;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.ProtocolParser;

class RpcProtocolFilter implements ProtocolFilter {

    public static final String RPC_CALL = "RPC_CALL";

    private final static Logger _log = Logger.getLogger(RpcProtocolFilter.class.getName());

    @Override
    public boolean execute(Context context) throws IOException {
        Xdr  xdr = (Xdr) context.removeAttribute(ProtocolParser.MESSAGE);

        if (xdr == null) {
            _log.log(Level.SEVERE, "Parser returns bad XDR");
            return false;
        }

        xdr.beginDecoding();

        int xid = xdr.xdrDecodeInt();
        int type = xdr.xdrDecodeInt();

        if (type == RpcMessageType.CALL) {
            RpcCall call = new RpcCall(xid, new GrizzlyXdrTransport(context));
            try {
               call.setXdr(xdr);

               /*
                * pass RPC call to the next filter in the chain
                */

               context.setAttribute(RPC_CALL, call);

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

        return true;
    }

}
