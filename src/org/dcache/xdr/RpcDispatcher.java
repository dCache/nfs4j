package org.dcache.xdr;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.apps.jportmap.OncRpcEmbeddedPortmap;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.JdbcFs;
import org.dcache.chimera.XMLconfig;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v3.HimeraNFSMountServerV2;
import org.dcache.chimera.nfs.v3.HimeraNFSServerV2;
import org.dcache.chimera.nfs.v4.DeviceManager;
import org.dcache.chimera.nfs.v4.HimeraNFS4Server;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;

public class RpcDispatcher implements ProtocolFilter {

    private final static Logger _log = Logger.getLogger(RpcDispatcher.class.getName());

    /*
     * List of registered RPC services
     */
    private final Map<Integer, RpcDispatchable> _programs =
            new HashMap<Integer, RpcDispatchable>();


    public RpcDispatcher() {

        try {
            XMLconfig config;
            new OncRpcEmbeddedPortmap(2000);

            OncRpcPortmapClient portmap =
                new OncRpcPortmapClient(InetAddress.getByName("127.0.0.1"));
            portmap.getOncRpcClient().setTimeout(2000);
            portmap.setPort(100005, 3, 6, 2049);
            portmap.setPort(100005, 1, 6, 2049);
            portmap.setPort(100003, 4, 6, 2049);
            config = new XMLconfig( new File("/home/tigran/eProjects/Chimera-hg/config.xml") );
            FileSystemProvider fs = new JdbcFs( config );
            ExportFile exports = new ExportFile( new File("/etc/exports"));
            HimeraNFSMountServerV2 ms = new HimeraNFSMountServerV2(exports, fs);
            HimeraNFSServerV2 nfs = new HimeraNFSServerV2(exports, fs);

            HimeraNFS4Server nfs4 = new HimeraNFS4Server(new DeviceManager(), fs, exports);

            _programs.put(100003, nfs4);
            _programs.put(100005, ms);
           // _programs.put(100003, nfs);

        } catch (Exception e) {
           _log.log(Level.SEVERE, "Failed to start Mount server:", e);
        }

    }

    @Override
    public boolean execute(Context context) throws IOException {


        RpcCall call = (RpcCall)context.getAttribute(RpcProtocolFilter.RPC_CALL);
        Xdr xdr = (Xdr)context.getAttribute(RpcProtocolFilter.RPC_XDR);

        int prog = call.getProgram();
        int vers = call.getProgramVersion();
        int proc = call.getProcedure();

        String msg = String.format("processing request prog=%d, vers=%d, proc=%d",
                prog, vers, proc);
        _log.log(Level.FINE, msg);

        RpcDispatchable program = _programs.get(Integer.valueOf(prog));
        if( program == null ) {
            call.failProgramUnavailable();
        }else{
            try {
                program.dispatchOncRpcCall(call, xdr);
            } catch (OncRpcException e) {
                _log.log(Level.SEVERE, "Failed to process RPC request:", e);
            }
        }

        return true;
    }

    @Override
    public boolean postExecute(Context context) throws IOException {
        return true;
    }

}
