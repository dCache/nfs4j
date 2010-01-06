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
import com.sun.grizzly.CallbackHandler;
import com.sun.grizzly.Context;
import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListenerAdapter;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.TCPConnectorHandler;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.util.ConnectionCloseHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OncRpcClient {

    private final static Logger _log = Logger.getLogger(OncRpcClient.class.getName());

    private final CountDownLatch clientReady = new CountDownLatch(1);
    private final Controller controller = new Controller();
    private int _port;
    private final InetAddress _address;
    private final ReplyQueue<Integer, RpcReply> _replyQueue =
            new ReplyQueue<Integer, RpcReply>();

    public OncRpcClient(InetAddress address, int protocol) {
        _address = address;
        init(address, protocol);
    }

    public OncRpcClient(InetAddress address, int protocol, int port) {
        _address = address;
        _port = port;
        init(address, protocol);
    }

    private void init(InetAddress address, int protocol) {

        TCPSelectorHandler tcp_handler = new TCPSelectorHandler(true);

        BaseSelectionKeyHandler selectionKeyHandler = new BaseSelectionKeyHandler();
        selectionKeyHandler.setConnectionCloseHandler(new ConnectionCloseHandler() {

            public void locallyClosed(SelectionKey sk) {
                System.out.println("Connection closed (locally)");
            }

            public void remotlyClosed(SelectionKey sk) {
                System.out.println("Remote peer closed connection");
            }
        });
        tcp_handler.setSelectionKeyHandler(selectionKeyHandler);

        controller.addSelectorHandler(tcp_handler);

        controller.addStateListener(
                new ControllerStateListenerAdapter() {

                    @Override
                    public void onReady() {
                        clientReady.countDown();
                        _log.log(Level.INFO, "Client ready");
                    }

                    @Override
                    public void onException(Throwable e) {
                        _log.log(Level.SEVERE, "Grizzly controller exception:" + e.getMessage());
                    }
                });

        final ProtocolFilter rpcFilter = new RpcParserProtocolFilter();
        final ProtocolFilter rpcProcessor = new RpcProtocolFilter(_replyQueue);

        final ProtocolChain protocolChain = new DefaultProtocolChain();
        protocolChain.addFilter(rpcFilter);
        protocolChain.addFilter(rpcProcessor);

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

    }

    public XdrTransport connect() throws IOException {

        new Thread(controller).start();

        try{
            clientReady.await();
        }catch(InterruptedException e) {
            _log.log(Level.SEVERE, "client initialization interrupted");
            throw new IOException(e.getMessage());
        }

        final TCPConnectorHandler connector_handler;
        connector_handler = (TCPConnectorHandler) controller.acquireConnectorHandler(Controller.Protocol.TCP);

        connector_handler.connect(new InetSocketAddress(_address, _port), (CallbackHandler<Context>) null);
         return new ClientTransport(connector_handler, _replyQueue);
    }

    public void close() throws IOException {
        controller.stop();
    }
}
