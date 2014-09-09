package org.dcache.testutils;

import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrEncodingStream;
import org.mockito.Mockito;

public class AssertXdr {
    public static void assertXdrEncodable (XdrAble xdrAble) {
        try {
            XdrEncodingStream outputStream = Mockito.mock(XdrEncodingStream.class);
            xdrAble.xdrEncode(outputStream); // should not blow up
        } catch (Exception e) {
            throw new AssertionError("object does not survive xdr encoding", e);
        }
    }
}
