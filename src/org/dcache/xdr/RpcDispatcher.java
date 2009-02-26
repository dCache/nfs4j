package org.dcache.xdr;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;

public class RpcDispatcher implements ProtocolFilter {

    private final static Logger _log = Logger.getLogger(RpcDispatcher.class.getName());

    /**
     * List of registered RPC services
     *
     */
    private final Map<Integer, RpcDispatchable> _programs;

    /**
     * Create new RPC dispatcher for given program.
     *
     * @param programs {@link Map}
     *     with a mapping between program number and program
     *     handler.
     *
     * @throws NullPointerException if programs is null
     */
    public RpcDispatcher(  Map<Integer, RpcDispatchable> programs  )
        throws NullPointerException{

        if( programs == null)
            throw new NullPointerException("Programs is NULL");

        _programs = programs;
    }

    @Override
    public boolean execute(Context context) throws IOException {


        RpcCall call = (RpcCall)context.getAttribute(RpcProtocolFilter.RPC_CALL);
        Xdr xdr = (Xdr)context.getAttribute(RpcProtocolFilter.RPC_XDR);

        int prog = call.getProgram();
        int vers = call.getProgramVersion();
        int proc = call.getProcedure();

        _log.log(Level.FINE, "processing request prog={0}, vers={1}, proc={2}",
                new Object[] {prog, vers, proc});

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
