package org.dcache.xdr;

import java.util.logging.Logger;

public class RpcAuthTypeNone implements RpcAuth, XdrAble {

    private final int _type =  RpcAuthType.NONE;

    private byte[] _data;
    private final static Logger _log = Logger.getLogger(RpcAuthTypeNone.class.getName());

    public void xdrDecode(XdrDecodingStream xdr) {
        int len = xdr.xdrDecodeInt();
        _data = new byte[len];
        xdr.xdrDecodeOpaque(_data, 0, len);
    }

    public int type() {
        return _type;
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException {
       xdr.xdrEncodeInt(_type);
       xdr.xdrEncodeOpaque(_data, 0, _data.length);
    }

}
