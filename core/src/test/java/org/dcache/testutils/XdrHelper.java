package org.dcache.testutils;

import org.dcache.oncrpc4j.xdr.Xdr;
import org.dcache.oncrpc4j.xdr.XdrAble;

public class XdrHelper {
    private XdrHelper() {}


    public static int calculateSize (XdrAble xdrAble) {
        try {
            Xdr xdr = new Xdr(128);
            xdr.beginEncoding();
            xdrAble.xdrEncode(xdr);
            xdr.endEncoding();
            return xdr.getBytes().length;
        } catch (Exception e) {
            throw new AssertionError("object does not survive xdr encoding", e);
        }
    }
}
