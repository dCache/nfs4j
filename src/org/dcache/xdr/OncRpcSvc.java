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

import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListenerAdapter;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.DefaultSelectionKeyHandler;
import com.sun.grizzly.PortRange;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.UDPSelectorHandler;
import com.sun.grizzly.util.DefaultThreadPool;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OncRpcSvc {

    private final static Logger _log = Logger.getLogger(OncRpcSvc.class.getName());

    private final Controller _controller = new Controller();
    private final CountDownLatch _serverReady = new CountDownLatch(1);

    /**
     * mapping of registered programs.
     */
    private final Map<OncRpcProgram, RpcDispatchable> _programs =
            new ConcurrentHashMap<OncRpcProgram, RpcDispatchable>();

    /**
     * Create a new server. Bind to all supported protocols.
     *
     * @param port TCP/UDP port to which service will he bound.
     */
    public OncRpcSvc(int port) {
        this(port, IpProtocolType.TCP | IpProtocolType.UDP);
    }

     /**
      * Create a new server.
      *
     * @param port TCP/UDP port to which service will he bound.
     * @param protocol to bind (tcp or udp)
     */
    public OncRpcSvc(int port, int protocol) {
        this(new PortRange(port), protocol);
    }

    /**
     * Create a new server.
     *
     * @param {@link PortRange} of TCP/UDP ports to which service will he bound.
     * @param protocol to bind (tcp or udp).
     */
    public OncRpcSvc(PortRange portRange, int protocol) {

        if( (protocol & (IpProtocolType.TCP | IpProtocolType.UDP)) == 0 ) {
            throw new IllegalArgumentException("TCP or UDP protocol have to be defined");
        }

        final ProtocolFilter protocolKeeper = new ProtocolKeeperFilter();
        final ProtocolFilter rpcFilter = new RpcParserProtocolFilter();
        final ProtocolFilter rpcProcessor = new RpcProtocolFilter();
        final ProtocolFilter rpcDispatcher = new RpcDispatcher(_programs);

        /*
         * By default, a SelectionKey will be active for 30 seconds.
         * If during that 30 seconds the client isn't pushing bytes
         * (or closing the connection), the SelectionKey will expire
         * and its channel closed.
         *
         * We set expire timeout to -1, which equal to 'never'.
         */
        DefaultSelectionKeyHandler keyHandler = new DefaultSelectionKeyHandler();
        keyHandler.setTimeout(-1);

        if((protocol & IpProtocolType.TCP) != 0) {
            final TCPSelectorHandler tcp_handler = new TCPSelectorHandler();
            tcp_handler.setPortRange(portRange);
            tcp_handler.setSelectionKeyHandler(keyHandler);
            _controller.addSelectorHandler(tcp_handler);
        }

        if((protocol & IpProtocolType.UDP) != 0) {
            final UDPSelectorHandler udp_handler = new UDPSelectorHandler();
            udp_handler.setPortRange(portRange);
            udp_handler.setSelectionKeyHandler(keyHandler);
            _controller.addSelectorHandler(udp_handler);
        }

        _controller.addStateListener(
                new ControllerStateListenerAdapter() {

                    @Override
                    public void onReady() {
                        _serverReady.countDown();
                    }
                });

        DefaultThreadPool defp;
        ExecutorService executorService = _controller.getThreadPool();

        if (executorService instanceof DefaultThreadPool) {
            defp = (DefaultThreadPool) executorService;
        } else {
            defp = new DefaultThreadPool();
            _controller.setThreadPool(defp);
        }
        defp.setInitialByteBufferSize(Xdr.MAX_XDR_SIZE);
        _controller.setReadThreadsCount(
                5);

        final ProtocolChain protocolChain = new DefaultProtocolChain();
        protocolChain.addFilter(protocolKeeper);
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

        _controller.setProtocolChainInstanceHandler(pciHandler);
    }

    /**
     * Start service.
     */
    public void start() throws IOException  {
        new Thread(_controller).start();
        try {
            _serverReady.await();
        } catch (InterruptedException ex) {
            _log.log(Level.SEVERE, "failed to start Controller", ex);
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Stop service.
     */
    public void stop() {
        try {
            _controller.stop();
        } catch (IOException e) {
           _log.log(Level.SEVERE, "failed to stop Controller", e);
        }
    }

    /**
     * Register a new PRC service. Existing registration will be overwritten.
     *
     * @param prog program number
     * @param handler RPC requests handler.
     */
    public void register(OncRpcProgram prog, RpcDispatchable handler) {
        _log.log(Level.INFO, "Registering new program {0} : {1}",
                new Object[] {prog, handler});
        _programs.put(prog, handler);
    }

    /**
     * Unregister program.
     *
     * @param prog
     */
    public void unregister(OncRpcProgram prog) {
        _log.log(Level.INFO, "Inregistering program {0}", prog);
        _programs.remove(prog);
    }

    /**
     * Get number of maximal concurrent threads.
     * @return thread number
     */
    public int getThreadCount() {
        return _controller.getReadThreadsCount();
    }

    /**
     * Set the maximal number of concurrent threads.
     * @param count
     */
    public void setThreadCount(int count) {
        _controller.setReadThreadsCount(count);
    }
}
