package org.dcache.xdr;

/**
 * Defines the interface for all classes that should be able to be
 * serialized into XDR streams, and deserialized or constructed from
 * XDR streams.
 */
public interface XdrAble {
    
    /**
     * Decodes -- that is: deserializes -- an object from a XDR stream in
     * compliance to RFC 1832.
     *
     * @param xdr XDR stream from which decoded information is retrieved.
     *
     * @throws XdrException if an ONC/RPC error occurs.
     */    
    public void xdrDecode(Xdr xdr) throws XdrException;
    
    /**
     * Encodes -- that is: serializes -- an object into a XDR stream in
     * compliance to RFC 1832.
     *
     * @param xdr XDR stream to which information is sent for encoding.
     * @throws XdrException if an ONC/RPC error occurs.
     */    
    public void xdrEncode(Xdr xdr) throws XdrException;    
}
