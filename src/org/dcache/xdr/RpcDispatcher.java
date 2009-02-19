package org.dcache.xdr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.util.OutputWriter;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RpcDispatcher implements ProtocolFilter {

    private final static Logger _log = Logger.getLogger(RpcDispatcher.class.getName());

    /*
     * List of registered RPC services
     */
    private final Map<Integer, RpcDispatchible> _programs =
            new HashMap<Integer, RpcDispatchible>();


    @Override
    public boolean execute(Context context) throws IOException {
        

        RpcCall call = (RpcCall)context.getAttribute(RpcProtocolFilter.RPC_CALL);
        RpcCallInfo info = (RpcCallInfo)context.getAttribute(RpcProtocolFilter.RPC_CALL_INFO);
        Xdr xdr = (Xdr)context.getAttribute(RpcProtocolFilter.RPC_XDR);

        int prog = call.getProgram();
        int vers = call.getProgramVersion();
        int proc = call.getProcedure();

        String msg = String.format("processing request prog=%d, vers=%d, proc=%d",
                proc, vers, proc);
        _log.log(Level.INFO, msg);

        RpcDispatchible program = _programs.get(Integer.valueOf(prog));
        if( program == null ) {
            call.reply( new RpcProgUnavailable(call.xid(), call.getAuthVerf()) );
        }

        return true;
    }

    @Override
    public boolean postExecute(Context context) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }
   
}
