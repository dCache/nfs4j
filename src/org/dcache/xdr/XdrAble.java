package org.dcache.xdr;

import java.io.IOException;

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
     * @throws OncRpcException if an ONC/RPC error occurs.
     */
    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException;

    /**
     * Encodes -- that is: serializes -- an object into a XDR stream in
     * compliance to RFC 1832.
     *
     * @param xdr XDR stream to which information is sent for encoding.
     * @throws OncRpcException if an ONC/RPC error occurs.
     */
    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException;
}
