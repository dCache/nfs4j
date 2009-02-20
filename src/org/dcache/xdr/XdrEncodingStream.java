package org.dcache.xdr;

import java.nio.ByteBuffer;

/**
 * Defines interface for encoding XDR stream. An encoding
 * XDR stream receives data in the form of Java data types and writes it to
 * a data sink (for instance, network or memory buffer) in the
 * platform-independent XDR format.
 */
public interface XdrEncodingStream {

    void beginEncoding();
    void endEncoding();
    void xdrEncodeInt(int value);
    void xdrEncodeIntVector(int[] ints);
    void xdrEncodeDynamicOpaque(byte [] opaque);
    void xdrEncodeOpaque(byte [] opaque, int len);
    void xdrEncodeOpaque(byte [] opaque, int offset, int len);
    void xdrEncodeBoolean(boolean bool);
    void xdrEncodeString(String str);
    void xdrEncodeLong(long value);
    ByteBuffer body();
    /*
     * Fake interface for compatibility with Remote Tea RPC library
     *
     */
}
