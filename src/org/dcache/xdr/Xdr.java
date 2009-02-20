package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Xdr implements XdrDecodingStream, XdrEncodingStream {

    private final static Logger _log = Logger.getLogger(Xdr.class.getName());
    private final ByteBuffer _body;

    /**
     * Build a new Xdr object with a buffer of given size.
     *
     * @param size of the buffer in bytes
     */
    public Xdr(int size) {
        this(ByteBuffer.allocate(size));
    }

    Xdr(ByteBuffer body) {
        _body = body;
        _body.order(ByteOrder.BIG_ENDIAN);
    }


    void startDecode() {
        _body.flip();
    }

    void stopDecode() {
        // NOP
    }
    void startEncode() {
        _body.clear().position(4);
    }

    void stopEncode() {
        int len = _body.position() -4 ;
        _body.putInt(0, len |= 0x80000000 );
        _body.flip();
    }


    void fill(ByteBuffer b) {
        _body.put(b);
    }

    /**
     *
     * @return true if Xdr message is complete.
     */
    boolean isComplete() {
        _log.log(Level.FINEST, "isComplete " + _body.remaining());
        return !_body.hasRemaining();
    }

    /**
     * Decodes (aka "deserializes") a "XDR int" value received from a
     * XDR stream. A XDR int is 32 bits wide -- the same width Java's "int"
     * data type has. This method is one of the basic methods all other
     * methods can rely on. Because it's so basic, derived classes have to
     * implement it.
     *
     * @return The decoded int value.
     */
    public int xdrDecodeInt() {
        int val = _body.getInt();
        _log.log(Level.FINEST, "Decoding int " + val);
        return val;
    }

    /**
     * Get next array of integers.
     *
     * @return
     */
    public int[] xdrDecodeIntVector() {

        int len = xdrDecodeInt();
        _log.log(Level.FINEST, "Decoding int array with len = " + len);

        int[] ints = new int[len];
        for (int i = 0; i < len; i++) {
            ints[i] = xdrDecodeInt();
        }
        return ints;
    }

    /**
     * Get next opaque data.  The decoded data
     * is always padded to be a multiple of four.
     *
     * @param buf buffer where date have to be stored
     * @param offset in the buffer.
     * @param len number of bytes to read.
     */
    public void xdrDecodeOpaque(byte[] buf, int offset, int len) {
        int padding = (4 - (len & 3)) & 3;
        _body.get(buf, offset, len);
        _body.position(_body.position() + padding);
    }

    /**
     * Get next String.
     *
     * @return
     */
    public String xdrDecodeString() {
        String ret;

        int len = xdrDecodeInt();
        _log.log(Level.FINEST, "Decoding string with len = " + len);

        if (len > 0) {
            byte[] bytes = new byte[len];
            xdrDecodeOpaque(bytes, 0, len);
            ret = new String(bytes);
        } else {
            ret = "";
        }

        return ret;
    }

    /**
     * Encodes (aka "serializes") a "XDR int" value and writes it down a
     * XDR stream. A XDR int is 32 bits wide -- the same width Java's "int"
     * data type has. This method is one of the basic methods all other
     * methods can rely on.
     */
    public void xdrEncodeInt(int value) {
        _body.putInt(value);
    }


    ByteBuffer body() {
        return _body;
    }

    void decode(XdrAble data) throws XdrException {
        data.xdrDecode(this);
    }

    void encode(XdrAble data) throws XdrException {
        data.xdrEncode(this);
    }

    /**
     * Encodes (aka "serializes") a vector of ints and writes it down
     * this XDR stream.
     *
     * @param value int vector to be encoded.
     *
     */
    public void xdrEncodeIntVector(int[] gids) {
        _body.putInt(gids.length);
        for (int i = 0; i < gids.length; i++) {
            _body.putInt( gids[i] );
        }
    }

    /**
     * Encodes (aka "serializes") a string and writes it down this XDR stream.
     *
     */
    public void xdrEncodeString(String string) {
        if( string == null ) string = "";
        xdrEncodeOpaque(string.getBytes(), 0, string.length());
    }

    private static final byte [] paddingZeros = { 0, 0, 0, 0 };

    /**
     * Encodes (aka "serializes") a XDR opaque value, which is represented
     * by a vector of byte values. Only the opaque value is encoded, but
     * no length indication is preceeding the opaque value, so the receiver
     * has to know how long the opaque value will be. The encoded data is
     * always padded to be a multiple of four. If the length of the given byte
     * vector is not a multiple of four, zero bytes will be used for padding.
     */
    public void xdrEncodeOpaque(byte[] bytes, int offset, int len) {
        int padding = (4 - (len & 3)) & 3;
        _body.putInt(len);
        _body.put(bytes, offset, len);
        _body.put(paddingZeros, 0, padding);
    }
}
