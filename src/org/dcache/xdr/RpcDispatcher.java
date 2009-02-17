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

    private final Map<Integer, Object> _programs = new HashMap<Integer, Object>();


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

        Object program = _programs.get(Integer.valueOf(prog));
        if( program == null ) {
            reply( new RpcProgUnavailable(call.xid(), call.getAuthVerf()), context);
        }

        return true;
    }

    @Override
    public boolean postExecute(Context context) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    
    private void reply(RpcReply reply, Context context) throws IOException {

        Xdr xdr = new Xdr(1024);

        try {            
            xdr.startEncode();
            xdr.encode(reply);
            xdr.stopEncode();
        }catch(XdrException e ) {
            throw new IOException(e.getMessage());
        }

        SelectableChannel channel = context.getSelectionKey().channel();
        ByteBuffer message = xdr.body();

        OutputWriter.flushChannel(channel, message);
    }

}
