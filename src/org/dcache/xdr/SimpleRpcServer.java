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

import com.sun.grizzly.BaseSelectionKeyHandler;
import com.sun.grizzly.Controller;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.TCPSelectorHandler;

import com.sun.grizzly.util.DefaultThreadPool;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.apps.jportmap.OncRpcEmbeddedPortmap;

public class SimpleRpcServer {

    private final static Logger _log = Logger.getLogger(SimpleRpcServer.class.getName());

    static final int DEFAULT_PORT = 1717;

    public static void main(String[] args) throws Exception {

        if( args.length > 1) {
            System.err.println("Usage: SimpleRpcServer <port>");
            System.exit(1);
        }

        int port = DEFAULT_PORT;
        if( args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Map<Integer, RpcDispatchable> programs = new HashMap<Integer, RpcDispatchable>();

        new OncRpcEmbeddedPortmap(2000);

        OncRpcPortmapClient portmap = new OncRpcPortmapClient(InetAddress
                    .getByName("127.0.0.1"));
        portmap.getOncRpcClient().setTimeout(2000);
        portmap.setPort(100017, 1, 6, port);

        RpcDispatchable dummy = new RpcDispatchable() {

            public void dispatchOncRpcCall(RpcCall call) throws OncRpcException, IOException {
                call.reply(XdrVoid.XDR_VOID);
            }

        };
        programs.put(100003, dummy);

        final ProtocolFilter rpcFilter = new RpcParserProtocolFilter();
        final ProtocolFilter rpcProcessor = new RpcProtocolFilter();
        final ProtocolFilter rpcDispatcher = new RpcDispatcher(programs);

        final Controller controller = new Controller();
        final TCPSelectorHandler tcp_handler = new TCPSelectorHandler();
        tcp_handler.setPort(port);
        tcp_handler.setSelectionKeyHandler(new BaseSelectionKeyHandler());

        controller.addSelectorHandler(tcp_handler);

        DefaultThreadPool defp;
        ExecutorService executorService = controller.getThreadPool();
        if( executorService instanceof DefaultThreadPool ) {
            defp = (DefaultThreadPool)executorService;
        } else {
            defp = new DefaultThreadPool();
            controller.setThreadPool( defp );
        }
        defp.setInitialByteBufferSize( Xdr.MAX_XDR_SIZE );
        controller.setReadThreadsCount(5);

        final ProtocolChain protocolChain = new DefaultProtocolChain();
        protocolChain.addFilter(rpcFilter);
        protocolChain.addFilter(rpcProcessor);
        protocolChain.addFilter(rpcDispatcher);

        ((DefaultProtocolChain) protocolChain).setContinuousExecution(true);

        ProtocolChainInstanceHandler pciHandler = new DefaultProtocolChainInstanceHandler() {
            @Override
            public ProtocolChain poll() {
                return protocolChain;
            }

            @Override
            public boolean offer(ProtocolChain pc) {
                return false;
            }
        };

        controller.setProtocolChainInstanceHandler(pciHandler);

        try {
            controller.start();
        } catch (Exception e) {
            _log.log(Level.SEVERE,"Exception in controller...", e);
        }
    }

}
