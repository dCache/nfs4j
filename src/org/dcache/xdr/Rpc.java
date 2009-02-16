package org.dcache.xdr;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Rpc implements XdrAble {

    private final static Logger _log = Logger.getLogger(Rpc.class.getName());

    private int _xid;
    private int _type;

    private RpcMsg _msg;   

    RpcMsg getMessage(Xdr xdr) {
        return _msg;
    }

    public void xdrDecode(Xdr xdr) throws XdrException {
        _xid = xdr.get_int();
        _type = xdr.get_int();
        _log.log(Level.FINEST, "type = " + _type);
        _log.log(Level.FINEST, "xid  = " + _xid);
    }

    public void xdrEncode(Xdr xdr) throws XdrException {
        xdr.put_int(_xid);
        xdr.put_int(_type);
        if(_type == RpcMessageType.REPLY ) {
            _msg.xdrEncode(xdr);
        }
    }

}
