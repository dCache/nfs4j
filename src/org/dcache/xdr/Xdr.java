package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Xdr {

	private final static Logger _log = Logger.getLogger(Xdr.class.getName());

	private ByteBuffer _body;

	Xdr(int size) {
		this(ByteBuffer.allocate(size));
	}

	Xdr(ByteBuffer body) {
		_body = body;
	}

	/**
	 * Get next integer.
	 * @return
	 */
	int get_int() {
		int val = _body.getInt();
		_log.log(Level.FINEST, "Decoding int " + val );
		return val;
	}

	/**
	 * Get next array of integers.
	 *
	 * @return
	 */
	int[] get_int_array() {

		int len = get_int();
		_log.log(Level.FINEST, "Decoding int array with len = " + len );

		int[] ints  = new int[len];
		for(int i = 0; i < len; i++) {
			ints[i] = get_int();
		}
		return ints;
	}

	/**
	 * Get next opaque data.  The decoded data
     * is always padded to be a multiple of four.
	 *
	 * @param buf buffer whare date have to be stored
	 * @param offset in the buffer.
	 * @param len number of bytes to read.
	 */
	void get_row_bytes(byte[] buf, int offset, int len) {
		int padding = (4 - (len & 3)) & 3;
		_body.get(buf, offset, len);
		_body.position( _body.position() + padding);
	}

	/**
	 * Get next String.
	 *
	 * @return
	 */
	String get_string() {
		String ret;
		
		int len = get_int();
		_log.log(Level.FINEST, "Decoding string with len = " + len );
		
		if( len > 0 ) {
			byte [] bytes = new byte[len];
			get_row_bytes(bytes, 0, len);
			ret = new String(bytes);
		}else{
			ret = "";
		}

		return ret;

	}

	void put_int(int xid) {
		_body.putInt(xid);
	}
}
