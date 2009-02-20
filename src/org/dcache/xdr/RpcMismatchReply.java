package org.dcache.xdr;

/**
 * The server is not running a compatible version
 * of the RPC protocol (RPC_MISMATCH).
 */
public class RpcMismatchReply extends RpcRejectedReply {

    private int _min;
    private int _max;
    
    public RpcMismatchReply(int min, int max) {
        _min = min;
        _max = max;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcRejectedReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        super.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcRejectStatus.RPC_MISMATCH);
        xdr.xdrEncodeInt(_min);
        xdr.xdrEncodeInt(_max);
    }

}
