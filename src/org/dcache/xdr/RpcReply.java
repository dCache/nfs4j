package org.dcache.xdr;

public class RpcReply extends RpcMsg {

    XdrAble _obj;
    
    public RpcReply(int xid) {
        super(xid, RpcMessageType.REPLY);
    }
    
    public void xdrDecode(Xdr xdr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageObject(XdrAble msg) {
        _obj = msg;
    }
    
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        xdr.put_int(super.xid());
        xdr.put_int(super.type());
        xdr.encode(_obj);
    }
}
