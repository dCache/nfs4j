package org.dcache.xdr;

import java.io.IOException;

public class XdrVoid implements XdrAble {

    /**
     * Encodes -- that is: serializes -- a void into a XDR stream in
     * compliance to RFC 1832.
     *
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException
    {
    }

    /**
     * Decodes -- that is: deserializes -- a void from a XDR stream in
     * compliance to RFC 1832.
     *
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException
    {
    }

    /**
     * Static <code>XdrVoid</code> instance, which can be used in cases
     * where no data is to be serialized or deserialized but some ONC/RPC
     * function expects a reference to a XDR-able object.
     */
    public final static XdrVoid XDR_VOID = new XdrVoid();

}
