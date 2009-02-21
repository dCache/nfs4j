package org.dcache.xdr;

import java.io.IOException;

/**
 *
 * Interface for RPC servers
 *
 */
public interface RpcDispatchable {

    public void dispatchOncRpcCall(RpcCall call, XdrDecodingStream xdr)
            throws OncRpcException, IOException;

}
