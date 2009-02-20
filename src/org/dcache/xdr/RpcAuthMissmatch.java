package org.dcache.xdr;

public class RpcAuthMissmatch extends RpcRejectedReply {

    private final int _status;

    public RpcAuthMissmatch(int status) {
        _status = status;
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        xdr.xdrEncodeInt(RpcRejectStatus.AUTH_ERROR);
        xdr.xdrEncodeInt(_status);
    }

}
