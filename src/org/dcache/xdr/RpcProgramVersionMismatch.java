package org.dcache.xdr;

public class RpcProgramVersionMismatch extends RpcAcceptedReply {

    private final int _min;
    private final int _max;

    public RpcProgramVersionMismatch(int min, int max) {
        _min = min;
        _max = max;
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        super.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcAccepsStatus.PROG_MISMATCH);
        xdr.xdrEncodeInt(_min);
        xdr.xdrEncodeInt(_max);
    }

}
