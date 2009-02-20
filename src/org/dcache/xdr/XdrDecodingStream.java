package org.dcache.xdr;

/**
 * Defines interface for decoding XDR stream. A decoding
 * XDR stream returns data in the form of Java data types which it reads
 * from a data source (for instance, network or memory buffer) in the
 * platform-independent XDR format.
 */
public interface XdrDecodingStream {


    void beginDecoding();
    void endDecoding();
    int xdrDecodeInt();
    int[] xdrDecodeIntVector();
    byte[] xdrDecodeDynamicOpaque();
    byte[] xdrDecodeOpaque(int size);
    void xdrDecodeOpaque(byte[] data, int offset, int len);
    boolean xdrDecodeBoolean();
    String xdrDecodeString();
    long xdrDecodeLong();

    /*
     * Fake interface for compatibility with Remote Tea RPC library
     *
     */
}
