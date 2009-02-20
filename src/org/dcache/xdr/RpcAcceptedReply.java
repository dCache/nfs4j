package org.dcache.xdr;

public abstract class RpcAcceptedReply extends RpcReply {

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        super.xdrEncode(xdr);
    }

}