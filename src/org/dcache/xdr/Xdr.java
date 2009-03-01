package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
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

    public Xdr(ByteBuffer body) {
        _body = body;
        _body.order(ByteOrder.BIG_ENDIAN);
    }

    public void beginDecoding() {
        _body.rewind();
    }

    public void endDecoding() {
        // NOP
    }

    public void beginEncoding() {
        /*
         * reserve space for record mark
         */
        _body.clear().position(4);
    }

    public void endEncoding() {
        int len = _body.position() -4 ;
        _log.log(Level.FINEST, "Encoded XDR size: " + len);
        /*
         * set record marker:
         */
        _body.putInt(0, len | 0x80000000 );
        _body.flip();
    }


    /**
     * Add bytes from provided buffer into internal byte buffer.
     * @param b {@link ByteBuffer} with data to be added
     */
    void fill(ByteBuffer b) {
        _body.put(b);
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
     * @return the array on integers
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
        _log.log(Level.FINEST, "padding zeros: " + padding);
        _body.get(buf, offset, len);
        _body.position(_body.position() + padding);
    }

    public void xdrDecodeOpaque(byte[] buf,  int len) {
        xdrDecodeOpaque(buf, 0, len);
    }

    public byte[] xdrDecodeOpaque(int len) {
        byte[] opaque = new byte[len];
        xdrDecodeOpaque(opaque, len);
        return opaque;
    }

    /**
     * Decodes (aka "deserializes") a XDR opaque value, which is represented
     * by a vector of byte values. The length of the opaque value to decode
     * is pulled off of the XDR stream, so the caller does not need to know
     * the exact length in advance. The decoded data is always padded to be
     * a multiple of four (because that's what the sender does).
     */
    public byte [] xdrDecodeDynamicOpaque() {
        int length = xdrDecodeInt();
        byte [] opaque = new byte[length];
        if ( length != 0 ) {
            xdrDecodeOpaque(opaque, 0, length);
        }
        return opaque;
    }

    /**
     * Get next String.
     *
     * @return decoded string
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

    public boolean xdrDecodeBoolean() {
        int bool = xdrDecodeInt();
        return bool != 0;
    }

    /**
     * Decodes (aka "deserializes") a long (which is called a "hyper" in XDR
     * babble and is 64&nbsp;bits wide) read from a XDR stream.
     *
     * @return Decoded long value.
     */
    public long xdrDecodeLong() {
        return (((long) xdrDecodeInt()) << 32) +
        ((xdrDecodeInt()) & 0x00000000FFFFFFFFl);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    //         Encoder
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Encodes (aka "serializes") a "XDR int" value and writes it down a
     * XDR stream. A XDR int is 32 bits wide -- the same width Java's "int"
     * data type has. This method is one of the basic methods all other
     * methods can rely on.
     */
    public void xdrEncodeInt(int value) {
		_log.log(Level.FINEST, "Ecoding int " + value);
        _body.putInt(value);
    }


    @Override
    public ByteBuffer body() {
        return _body;
    }

    /**
     * Encodes (aka "serializes") a vector of ints and writes it down
     * this XDR stream.
     *
     * @param value int vector to be encoded.
     *
     */
    public void xdrEncodeIntVector(int[] value) {
		_log.log(Level.FINEST, "Ecoding int array " + Arrays.toString(value));
        _body.putInt(value.length);
        for (int i = 0; i < value.length; i++) {
            _body.putInt( value[i] );
        }
    }

    /**
     * Encodes (aka "serializes") a string and writes it down this XDR stream.
     *
     */
    public void xdrEncodeString(String string) {
		_log.log(Level.FINEST, "Encode String:  " + string);
        if( string == null ) string = "";
        xdrEncodeDynamicOpaque(string.getBytes());
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
		_log.log(Level.FINEST, "Encode Opaque, len = " + len);
        int padding = (4 - (len & 3)) & 3;
        _body.put(bytes, offset, len);
        _body.put(paddingZeros, 0, padding);
    }

    public void xdrEncodeOpaque(byte[] bytes, int len) {
        xdrEncodeOpaque(bytes, 0, len);
    }

    /**
     * Encodes (aka "serializes") a XDR opaque value, which is represented
     * by a vector of byte values. The length of the opaque value is written
     * to the XDR stream, so the receiver does not need to know
     * the exact length in advance. The encoded data is always padded to be
     * a multiple of four to maintain XDR alignment.
     *
     */
    public void xdrEncodeDynamicOpaque(byte [] opaque) {
        xdrEncodeInt(opaque.length);
        xdrEncodeOpaque(opaque, 0, opaque.length);
    }

    public void xdrEncodeBoolean(boolean bool) {
        xdrEncodeInt( bool ? 1 : 0);
    }

    /**
     * Encodes (aka "serializes") a long (which is called a "hyper" in XDR
     * babble and is 64&nbsp;bits wide) and write it down this XDR stream.
     */
    public void xdrEncodeLong(long value) {
        xdrEncodeInt((int)(value >>> 32));
        xdrEncodeInt((int)(value & 0xFFFFFFFF));
    }
}
