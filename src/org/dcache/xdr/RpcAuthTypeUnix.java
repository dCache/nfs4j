package org.dcache.xdr;

import java.util.logging.Logger;
import java.util.Arrays;

public class RpcAuthTypeUnix implements RpcAuth, XdrAble {

    private final int _type =  RpcAuthType.UNIX;
    
    private int _len;
    private int _uid;
    private int _gid;
    private int _gids[];
    private int _stamp;
    private String _machine;

    private final static Logger _log = Logger.getLogger(RpcAuthTypeUnix.class.getName());

    RpcAuthTypeUnix() {}

    public void xdrDecode(Xdr xdr) {

        _len = xdr.xdrDecodeInt();
        _stamp = xdr.xdrDecodeInt();
        _machine = xdr.xdrDecodeString();
        _uid = xdr.xdrDecodeInt();
        _gid = xdr.xdrDecodeInt();
        _gids = xdr.xdrDecodeIntVector();
    }

    public int type() {
        return _type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Host: ").append(_machine).append("\n");
        sb.append("timestamp: ").append(_stamp).append("\n");
        sb.append("uid: ").append(_uid).append("\n");
        sb.append("gid: ").append(_gid).append("\n");
        sb.append("gids: ").append(Arrays.toString(_gids)).append("\n");

        return sb.toString();
    }

    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
       xdr.xdrEncodeInt(_type);
       xdr.xdrEncodeInt(_len);
       xdr.xdrEncodeInt(_stamp);
       xdr.xdrEncodeString(_machine);
       xdr.xdrEncodeInt(_uid);
       xdr.xdrEncodeInt(_gid);
       xdr.xdrEncodeIntVector(_gids);        
    }

}
