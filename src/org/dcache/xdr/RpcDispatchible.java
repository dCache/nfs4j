package org.dcache.xdr;

/**
 *
 * Interface for RPC servers
 *
 */
public interface RpcDispatchible {

    public void dispatchOncRpcCall(RpcCall call, XdrDecodingStream xdr);

}
