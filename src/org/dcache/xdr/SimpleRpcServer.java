package org.dcache.xdr;

import com.sun.grizzly.BaseSelectionKeyHandler;
import com.sun.grizzly.Controller;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.TCPSelectorHandler;

import java.util.logging.Logger;
import java.util.logging.Level;


public class SimpleRpcServer {

    private final static Logger _log = Logger.getLogger(SimpleRpcServer.class.getName());

    static final int DEFAULT_PORT = 2049;

    public static void main(String[] args) {

        _log.log(Level.CONFIG, "starting on:" + DEFAULT_PORT );

        final ProtocolFilter rpcFilter = new RpcParserProtocolFilter();
        final ProtocolFilter rpcProcessor = new RpcProtocolFilter();
        final ProtocolFilter rpcDispatcher = new RpcDispatcher();

        final Controller controller = new Controller();
        final TCPSelectorHandler tcp_handler = new TCPSelectorHandler();
        tcp_handler.setPort(DEFAULT_PORT);
        tcp_handler.setSelectionKeyHandler(new BaseSelectionKeyHandler());
                
        controller.addSelectorHandler(tcp_handler);
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
