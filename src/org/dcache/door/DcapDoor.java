package org.dcache.door;

import com.sun.grizzly.Controller;
import com.sun.grizzly.DefaultPipeline;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.Pipeline;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.TCPSelectorHandler;

public class DcapDoor {


    static final int DEFAULT_PORT = 2317;

    public static void main(String[] args) {

        System.out.println("starting on:" + DEFAULT_PORT );

        final ProtocolFilter asciiCommandParser = new AsciiCommandProtocolFilter();
        final ProtocolFilter dcap = new DcapProtocolFilter();

        final TCPSelectorHandler tcp_handler = new TCPSelectorHandler();
        tcp_handler.setPort(DEFAULT_PORT);
        final Controller controller = new Controller();
        controller.setSelectorHandler(tcp_handler);

        Pipeline pipeline = new DefaultPipeline();
        pipeline.setMaxThreads(5);
        controller.setPipeline(pipeline);

        final ProtocolChain protocolChain = new DefaultProtocolChain();
        protocolChain.addFilter(asciiCommandParser);
        protocolChain.addFilter(dcap);
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
            System.out.println("Exception in controller...");
        }
    }

}
