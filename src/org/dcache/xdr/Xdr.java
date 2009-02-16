package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Xdr {

    private final static Logger _log = Logger.getLogger(Xdr.class.getName());
    private final ByteBuffer _body;
    private boolean _inDecode = false;

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
     * Get next integer.
     * @return
     */
    int get_int() {
        int val = _body.getInt();
        _log.log(Level.FINEST, "Decoding int " + val);
        return val;
    }

    /**
     * Get next array of integers.
     *
     * @return
     */
    int[] get_int_array() {

        int len = get_int();
        _log.log(Level.FINEST, "Decoding int array with len = " + len);

        int[] ints = new int[len];
        for (int i = 0; i < len; i++) {
            ints[i] = get_int();
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
    void get_row_bytes(byte[] buf, int offset, int len) {
        int padding = (4 - (len & 3)) & 3;
        _body.get(buf, offset, len);
        _body.position(_body.position() + padding);
    }

    /**
     * Get next String.
     *
     * @return
     */
    String get_string() {
        String ret;

        int len = get_int();
        _log.log(Level.FINEST, "Decoding string with len = " + len);

        if (len > 0) {
            byte[] bytes = new byte[len];
            get_row_bytes(bytes, 0, len);
            ret = new String(bytes);
        } else {
            ret = "";
        }

        return ret;

    }

    void put_int(int xid) {
        _body.putInt(xid);
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
}
