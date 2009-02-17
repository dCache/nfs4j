package org.dcache.xdr;

public abstract class RpcAcceptedReply extends RpcReply {

    public RpcAcceptedReply(int xid) {
        super(xid);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcReply#xdrDecode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrDecode(Xdr xdr) {
        // TODO Auto-generated method stub
        super.xdrDecode(xdr);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        super.xdrEncode(xdr);
        xdr.put_int(RpcReplyStats.MSG_ACCEPTED);
    }

}