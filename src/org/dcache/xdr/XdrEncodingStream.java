package org.dcache.xdr;

/**
 * Defines interface for encoding XDR stream. An encoding
 * XDR stream receives data in the form of Java data types and writes it to
 * a data sink (for instance, network or memory buffer) in the
 * platform-independent XDR format.
 */
public interface XdrEncodingStream {
    /*
     * Fake interface for compatibility with Remote Tea RPC library
     *
     */
}
