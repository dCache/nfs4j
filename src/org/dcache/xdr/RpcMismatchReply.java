package org.dcache.xdr;

public class RpcMismatchReply extends RpcRejectedReply {

    private int _min;
    private int _max;

    public RpcMismatchReply(int xid, int min, int max) {
        super(xid);
        _min = min;
        _max = max;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcRejectedReply#xdrDecode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrDecode(Xdr xdr) {
        // TODO Auto-generated method stub
        super.xdrDecode(xdr);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcRejectedReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        super.xdrEncode(xdr);
        xdr.put_int(RpcRejectStatus.RPC_MISMATCH);
        xdr.put_int(_min);
        xdr.put_int(_max);
    }

}
