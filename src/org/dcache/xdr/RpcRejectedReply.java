package org.dcache.xdr;

public abstract class RpcRejectedReply extends RpcReply {


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
