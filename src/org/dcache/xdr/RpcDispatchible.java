package org.dcache.xdr;

import com.sun.grizzly.Context;


/**
 *
 * Interface for RPC servers
 *
 */
public interface RpcDispatchible {

    public void dispatchOncRpcCall(RpcCall call, XdrDecodingStream xdr, RpcCallInfo info);

}
