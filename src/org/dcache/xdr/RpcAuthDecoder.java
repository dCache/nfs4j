package org.dcache.xdr;

import java.util.logging.Logger;
import java.util.logging.Level;

public class RpcAuthDecoder {

    private final static Logger _log = Logger.getLogger(RpcAuthDecoder.class.getName());

    public static RpcAuth getRpcAuth(Xdr xdr) {

        int type = xdr.xdrDecodeInt();

        _log.log(Level.FINEST, "decoding auth type: " + type);

        if( type == 1) {
//          return new RpcAuthTypeUnix(xdr);
        }
        throw new UnsupportedOperationException("Auth type not supported");

    }

}
