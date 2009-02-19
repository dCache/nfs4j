package org.dcache.xdr;

public class RpcProgUnavailable extends RpcAcceptedReply {

    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        super.xdrEncode(xdr);
        xdr.put_int(RpcAccepsStatus.PROG_UNAVAIL);
    }

}
