package org.dcache.xdr;

import java.nio.ByteBuffer;


/**
 *
 * The same as Xdr, but does not reserve place
 * for the RPC fragment marker
 */
public class XdrBuffer extends Xdr {

    /**
     * Build a new Xdr object with a buffer of given size.
     *
     * @param size of the buffer in bytes
     */
    public XdrBuffer(int size) {
        this(ByteBuffer.allocate(size));
    }

    public XdrBuffer(ByteBuffer body) {
        super(body);
    }

    @Override
    public void beginDecoding() {
    }

    @Override
    public void endDecoding() {
        // NOP
    }

    @Override
    public void beginEncoding() {
    }

    @Override
    public void endEncoding() {
        super.body().flip();
    }



}
