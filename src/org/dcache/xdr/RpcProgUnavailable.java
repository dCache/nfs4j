package org.dcache.xdr;

public class RpcProgUnavailable extends RpcAcceptedReply {

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        super.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcAccepsStatus.PROG_UNAVAIL);
    }

}
