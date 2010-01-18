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

package org.dcache.chimera.nfs.v4;

import org.dcache.xdr.*;

import java.io.File;
import java.net.InetAddress;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.OncRpcProtocols;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.JdbcFs;
import org.dcache.chimera.XMLconfig;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v3.MountServer;
import org.dcache.chimera.nfs.v4.mover.NFSProtocol_4;
import org.dcache.xdr.portmap.OncRpcEmbeddedPortmap;


public class Main {

    private final static Logger _log = Logger.getLogger(Main.class.getName());

    static final int DEFAULT_PORT_MDS = 2049;
    static final int DEFAULT_PORT_DS = 2052;

    public static void main(String[] args) throws Exception {

        if( args.length != 2) {
            System.err.println("Usage: Main <chimera-config> <mds|ds>");
            System.exit(1);
        }

        boolean mds = args[1].equals("mds");

        XMLconfig config = new XMLconfig(new File(args[0]));
        FileSystemProvider fs = new JdbcFs(config);

        OncRpcSvc service;
        int port;
        if( mds ) {
            port  = DEFAULT_PORT_MDS;
            service = new OncRpcSvc(port);
            _log.log(Level.CONFIG, "starting MDS on: {0}", port);

            new OncRpcEmbeddedPortmap(2000);

            OncRpcPortmapClient portmap = new OncRpcPortmapClient(InetAddress
                    .getByName("127.0.0.1"));
            portmap.getOncRpcClient().setTimeout(2000);
            if ( !portmap.setPort(100005, 3, OncRpcProtocols.ONCRPC_TCP, 2049) ) {
                _log.log(Level.SEVERE, "Failed to register mountv1 service within portmap.");
            }
            if (!portmap.setPort(100005, 3, OncRpcProtocols.ONCRPC_UDP, 2049)) {
                _log.log(Level.SEVERE, "Failed to register mountv1 service within portmap.");
            }
            if(!portmap.setPort(100005, 1, OncRpcProtocols.ONCRPC_TCP, 2049)) {
                _log.log(Level.SEVERE, "Failed to register mountv3 service within portmap.");
            }
            if(!portmap.setPort(100005, 1, OncRpcProtocols.ONCRPC_UDP, 2049)) {
                _log.log(Level.SEVERE, "Failed to register mountv3 service within portmap.");
            }

            if( !portmap.setPort(100003, 4, OncRpcProtocols.ONCRPC_TCP, 2049) ) {
                _log.log(Level.SEVERE, "Failed to register NFSv4 service within portmap.");
            }

            ExportFile exports = new ExportFile(new File("/etc/exports"));
            MountServer ms = new MountServer(exports, fs);

            NFSServerV41 nfs4 = new NFSServerV41(new DeviceManager(),
                    fs, exports);

            service.register(100003, nfs4);
            service.register(100005, ms);

        }else{
            port = DEFAULT_PORT_DS;
            service = new OncRpcSvc(port);
            _log.log(Level.INFO, "starting DS on: {0}", port );
            NFSProtocol_4 ds = new NFSProtocol_4(fs, new File("/tmp/pNFS"));
            service.register(100003, ds);
        }

        service.start();
    }

}
