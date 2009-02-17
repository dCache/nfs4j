package org.dcache.xdr;

public abstract class RpcRejectedReply extends RpcReply {

    public RpcRejectedReply(int xid) {
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
        // TODO Auto-generated method stub
        super.xdrEncode(xdr);
        xdr.put_int(RpcReplyStats.MSG_DENIED);        
    }

}
