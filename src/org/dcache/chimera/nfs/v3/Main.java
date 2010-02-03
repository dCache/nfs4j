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
package org.dcache.chimera.nfs.v3;



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
import org.dcache.chimera.nfs.v3.xdr.mount_prot;
import org.dcache.chimera.nfs.v3.xdr.nfs3_prot;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.portmap.OncRpcEmbeddedPortmap;

public class Main {

    private final static Logger _log = Logger.getLogger(Main.class.getName());

    static final int DEFAULT_PORT = 2049;

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            _log.severe("Usage: Main <config>");
            System.exit(1);
        }

        XMLconfig config = new XMLconfig(new File(args[0]));
        FileSystemProvider fs = new JdbcFs(config);

        _log.log(Level.CONFIG, "starting NFSv3 on: {0}", DEFAULT_PORT);

        new OncRpcEmbeddedPortmap(2000);

        OncRpcPortmapClient portmap = new OncRpcPortmapClient(InetAddress.getByName("127.0.0.1"));
        portmap.getOncRpcClient().setTimeout(2000);

        if (!portmap.setPort(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V3, OncRpcProtocols.ONCRPC_TCP, 2049)) {
            _log.log(Level.SEVERE, "Failed to register mountv1 service within portmap.");
        }

        if (!portmap.setPort(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V3, OncRpcProtocols.ONCRPC_UDP, 2049)) {
            _log.log(Level.SEVERE, "Failed to register mountv1 service within portmap.");
        }

        if (!portmap.setPort(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V1, OncRpcProtocols.ONCRPC_TCP, 2049)) {
            _log.log(Level.SEVERE, "Failed to register mountv3 service within portmap.");
        }

        if (!portmap.setPort(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V1, OncRpcProtocols.ONCRPC_UDP, 2049)) {
            _log.log(Level.SEVERE, "Failed to register mountv3 service within portmap.");
        }

        if (!portmap.setPort(nfs3_prot.NFS_PROGRAM, nfs3_prot.NFS_V3, OncRpcProtocols.ONCRPC_TCP, 2049)) {
            _log.log(Level.SEVERE, "Failed to register NFSv3 service within portmap.");
        }

        if (!portmap.setPort(nfs3_prot.NFS_PROGRAM, nfs3_prot.NFS_V3, OncRpcProtocols.ONCRPC_UDP, 2049)) {
            _log.log(Level.SEVERE, "Failed to register NFSv3 service within portmap.");
        }

        ExportFile exports = new ExportFile(new File("/etc/exports"));
        MountServer ms = new MountServer(exports, fs);

        NfsServerV3 nfs3 = new NfsServerV3(exports, fs);

        OncRpcSvc service = new OncRpcSvc(DEFAULT_PORT);
        service.register(new OncRpcProgram(nfs3_prot.NFS_PROGRAM, nfs3_prot.NFS_V3), nfs3);
        service.register(new OncRpcProgram(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V3), ms);
        service.start();
    }

}
