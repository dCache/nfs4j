package org.dcache.xdr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;

public class RpcDispatcher implements ProtocolFilter {

    private final static Logger _log = Logger.getLogger(RpcDispatcher.class.getName());

    /*
     * List of registered RPC services
     */
    private final Map<Integer, RpcDispatchible> _programs =
            new HashMap<Integer, RpcDispatchible>();


    public RpcDispatcher() {
        _programs.put(100003, new RpcDispatchible() {

            @Override
            public void dispatch(RpcCall call, Context context, RpcCallInfo info) {

                call.reply(new RpcProgramVersionMismatch(2, 3));
            }

        });
    }

    @Override
    public boolean execute(Context context) throws IOException {
        

        RpcCall call = (RpcCall)context.getAttribute(RpcProtocolFilter.RPC_CALL);
        RpcCallInfo info = (RpcCallInfo)context.getAttribute(RpcProtocolFilter.RPC_CALL_INFO);
        Xdr xdr = (Xdr)context.getAttribute(RpcProtocolFilter.RPC_XDR);

        int prog = call.getProgram();
        int vers = call.getProgramVersion();
        int proc = call.getProcedure();

        String msg = String.format("processing request prog=%d, vers=%d, proc=%d",
                prog, vers, proc);
        _log.log(Level.INFO, msg);

        RpcDispatchible program = _programs.get(Integer.valueOf(prog));
        if( program == null ) {
            call.reply( new RpcProgUnavailable() );
        }else{
            program.dispatch(call, context, info);
        }

        return true;
    }

    @Override
    public boolean postExecute(Context context) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }
   
}
