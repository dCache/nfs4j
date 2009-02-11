package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Xdr {

	private final ByteBuffer _buffer;

    /**
     * Build a new Xdr object with a buffer of given size
     *
     * @param size	of the buffer in bytes
     */
	public Xdr(int size) {
		_buffer = ByteBuffer.allocate(size);
		// internal java byte order is BIG ENDIAN
		_buffer.order(ByteOrder.BIG_ENDIAN);
	}


	void fill(ByteBuffer b) {

		_buffer.put(b);

	}


	boolean isComplete() {
		return !_buffer.hasRemaining();
	}

}
