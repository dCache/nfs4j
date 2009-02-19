package org.dcache.xdr;

import com.sun.grizzly.Context;


/**
 * 
 * Interface for RPC servers
 *
 */
public interface RpcDispatchible {
    
    public void dispatch(RpcCall call, Context context, RpcCallInfo info);
    
}
