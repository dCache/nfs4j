package org.dcache.xdr;

public class MismatchInfo implements XdrAble {

    private int _min;
    private int _max;

    public MismatchInfo(int min, int max) {
        _min = min;
        _max = max;
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        xdr.xdrEncodeInt(_min);
        xdr.xdrEncodeInt(_max);
    }

    @Override
    public void xdrDecode(XdrDecodingStream xdr) {
        _min = xdr.xdrDecodeInt();
        _max = xdr.xdrDecodeInt();
    }

}
