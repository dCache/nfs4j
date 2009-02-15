package org.dcache.xdr;

/**
 * Given that a call message was accepted, the following is the
 * status of an attempt to call a remote procedure.
 */
public interface  RpcAccepsStatus {

    /**
     * RPC executed successfully
     */
    public static final int SUCCESS  = 0;
    /**
     * Remote hasn't exported program.
     */
    public static final int PROG_UNAVAIL = 1;
    /**
     * Remote can't support version #.
     */
    public static final int PROG_MISMATCH = 2;
    /**
     * Program can't support procedure.
     */
    public static final int PROC_UNAVAIL = 3;
    /**
     * Procedure can't decode params.
     */
    public static final int GARBAGE_ARGS = 4;
}
