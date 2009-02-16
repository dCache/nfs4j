package org.dcache.xdr;

import java.util.logging.Logger;
import java.util.Arrays;

public class RpcAuthTypeNone implements RpcAuth, XdrAble {

    private final int _type =  RpcAuthType.NONE;
    
    private byte[] _data;
    private final static Logger _log = Logger.getLogger(RpcAuthTypeNone.class.getName());

    public void xdrDecode(Xdr xdr) {
        int len = xdr.get_int();
        _data = new byte[len];
        xdr.get_raw_bytes(_data, 0, len);
    }

    public int type() {
        return _type;
    }

    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
       xdr.put_int(_type);
       xdr.put_raw_bytes(_data, 0, _data.length);
    }
}
