package org.dcache.xdr;

public class RpcReply extends RpcMsg {
    
    public RpcReply(int xid) {
        super(xid, RpcMessageType.REPLY);
    }
    
    public void xdrDecode(Xdr xdr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        xdr.put_int(super.xid());
        xdr.put_int(super.type());
    }
}
