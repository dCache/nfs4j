package org.dcache.xdr;

public abstract class RpcMsg implements XdrAble {

    private final int _xid;
    private final int _type;
    
    public RpcMsg(int xid, int type) {
        _xid = xid;
        _type = type;
    }
    
    public int xid() {
        return _xid;
    }
    
    public int type() {
        return _type;
    }

}
