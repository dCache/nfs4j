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
    private final Map<Integer, RpcDispatchable> _programs =
            new HashMap<Integer, RpcDispatchable>();


    public RpcDispatcher() {
        _programs.put(100003, new RpcDispatchable() {

            @Override
            public void dispatchOncRpcCall(RpcCall call, XdrDecodingStream xdr) {

                call.reply(new RpcProgramVersionMismatch(2, 3));
            }

        });
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
        _log.log(Level.INFO, msg);

        RpcDispatchable program = _programs.get(Integer.valueOf(prog));
        if( program == null ) {
            call.reply( new RpcProgUnavailable() );
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
        // TODO Auto-generated method stub
        return false;
    }

}
