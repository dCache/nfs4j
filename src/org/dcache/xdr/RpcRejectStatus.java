package org.dcache.xdr;

/**
 * Reasons why a call message was rejected.
 */
public interface RpcRejectStatus {

    /**
     * RPC version number != 2.
     */
    public static final int RPC_MISMATCH = 0;
    /**
     * Remote can't authenticate caller.
     */
    public static final int AUTH_ERROR = 1;
}
