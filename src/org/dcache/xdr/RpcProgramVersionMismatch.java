package org.dcache.xdr;

public class RpcProgramVersionMismatch extends RpcAcceptedReply {
    
    private final int _min;
    private final int _max;
    
    public RpcProgramVersionMismatch(int min, int max) {
        _min = min;
        _max = max;
    }
    
    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        super.xdrEncode(xdr);
        xdr.put_int(RpcAccepsStatus.PROG_MISMATCH);
        xdr.put_int(_min);
        xdr.put_int(_max);
    }

}
