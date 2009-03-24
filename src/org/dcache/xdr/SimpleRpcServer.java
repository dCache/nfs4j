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

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.apps.jportmap.OncRpcEmbeddedPortmap;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.JdbcFs;
import org.dcache.chimera.XMLconfig;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v3.HimeraNFSMountServerV2;
import org.dcache.chimera.nfs.v4.DeviceManager;
import org.dcache.chimera.nfs.v4.HimeraNFS4Server;
import org.dcache.chimera.nfs.v4.mover.NFSProtocol_4;


public class SimpleRpcServer {

    private final static Logger _log = Logger.getLogger(SimpleRpcServer.class.getName());

    static final int DEFAULT_PORT_MDS = 2049;
    static final int DEFAULT_PORT_DS = 2052;

    public static void main(String[] args) throws Exception {

        if( args.length != 1) {
            System.err.println("Usage: SimpleRpcServer <mds|ds>");
            System.exit(1);
        }

        boolean mds = args[0].equals("mds");

        Map<Integer, RpcDispatchable> programs = new HashMap<Integer, RpcDispatchable>();
        XMLconfig config = new XMLconfig(new File("/home/tigran/eProjects/Chimera-hg/config.xml"));
        FileSystemProvider fs = new JdbcFs(config);

        int port;
        if( mds ) {
            port  = DEFAULT_PORT_MDS;
            _log.log(Level.CONFIG, "starting MDS on: {0}", port);

            new OncRpcEmbeddedPortmap(2000);

            OncRpcPortmapClient portmap = new OncRpcPortmapClient(InetAddress
                    .getByName("127.0.0.1"));
            portmap.getOncRpcClient().setTimeout(2000);
            portmap.setPort(100005, 3, 6, 2049);
            portmap.setPort(100005, 1, 6, 2049);
            portmap.setPort(100003, 4, 6, 2049);


            ExportFile exports = new ExportFile(new File("/etc/exports"));
            HimeraNFSMountServerV2 ms = new HimeraNFSMountServerV2(exports, fs);

            HimeraNFS4Server nfs4 = new HimeraNFS4Server(new DeviceManager(),
                    fs, exports);

            programs.put(100003, nfs4);
            programs.put(100005, ms);

        }else{
            port = DEFAULT_PORT_DS;
            _log.log(Level.CONFIG, "starting DS on: {0}", port );
            NFSProtocol_4 ds = new NFSProtocol_4(fs, new File("/tmp/pNFS"));
            programs.put(100003, ds);
        }

        final ProtocolFilter rpcFilter = new RpcParserProtocolFilter();
        final ProtocolFilter rpcProcessor = new RpcProtocolFilter();
        final ProtocolFilter rpcDispatcher = new RpcDispatcher(programs);

        final Controller controller = new Controller();
        final TCPSelectorHandler tcp_handler = new TCPSelectorHandler();
        tcp_handler.setPort(port);
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
