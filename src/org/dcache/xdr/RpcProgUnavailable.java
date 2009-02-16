package org.dcache.xdr;

public class RpcProgUnavailable extends RpcReply {

    private final static int _error = RpcAccepsStatus.PROG_UNAVAIL;
    private final RpcAuth _auth;
    
    
    public RpcProgUnavailable(int xid, RpcAuth auth) {
        super(xid);
        _auth = auth;
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
        xdr.put_int(super.xid());
        xdr.put_int(super.type());
        xdr.put_int(RpcReplyStats.MSG_ACCEPTED);
        _auth.xdrEncode(xdr);
        xdr.put_int(_error);
    }

}
