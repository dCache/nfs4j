package org.dcache.xdr;

import com.sun.grizzly.ProtocolParser;
import java.nio.ByteBuffer;

import java.util.logging.Logger;
import java.util.logging.Level;

public class RpcProtocolPaser implements ProtocolParser<Xdr> {

	private final static Logger _log = Logger.getLogger(RpcProtocolPaser.class.getName());
	private final static int MAX_XDR_SIZE = 8192;

	/**
	 * Xdr which we try to construct.
	 */
	private Xdr _xdr = null;
	private int _messageLen = 0;
	private boolean _eom = true;
	/**
	 * did we start to read message data
	 */
	private boolean _inMessage = false;
	private boolean _inHeader = false;
	ByteBuffer _buffer;
	ByteBuffer _tmp = ByteBuffer.allocate(4);

	/**
	 *
	 * @see com.sun.grizzly.ProtocolParser#isExpectingMoreData()
	 */
	@Override
	public boolean isExpectingMoreData() {
		_log.log(Level.FINEST, "enter: isExpectingMoreData");
		boolean rc = _inMessage || _inHeader;
		_log.log(Level.FINEST, "left: isExpectingMoreData " + rc);
		return rc;
	}

	/**
	 *
	 * @see com.sun.grizzly.ProtocolParser#hasMoreBytesToParse()
	 */
	@Override
	public boolean hasMoreBytesToParse() {
		_log.log(Level.FINEST, "enter: hasMoreBytesToParse");
		boolean rc = _buffer != null && _buffer.hasRemaining();
		_log.log(Level.FINEST, "left: hasMoreBytesToParse " + rc);
		return rc;
	}

	/**
	 *
	 * @see com.sun.grizzly.ProtocolParser#getNextMessage()
	 */
	@Override
	public Xdr getNextMessage() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @see com.sun.grizzly.ProtocolParser#hasNextMessage()
	 */
	@Override
	public boolean hasNextMessage() {

		if (_buffer == null) {
			_log.log(Level.FINEST, "hasNextMessage false");
			return false;
		}

		// TODO: check for size 
		if (!_inMessage) {

			while(_tmp.hasRemaining() && _buffer.hasRemaining() ) {
				_tmp.put(_buffer.get());
			}

			if( _tmp.remaining() > 0 ) {
				_log.log(Level.FINEST, "hasNextMessage false (not enough data) " + _tmp.remaining());
				_inHeader = true;
				return false;
			}

			_inHeader = false;
			_inMessage = true;
			_tmp.flip();
			int size = _tmp.getInt();
			_tmp.clear();
			_log.log(Level.FINEST, "expected message size = " + size);
			if( size > MAX_XDR_SIZE ) {
				_log.log(Level.INFO, "hasNextMessage false, overflow size=" + size );
				return false;
			}
			_xdr = new Xdr(size);
			_xdr.fill(_buffer);

		}

		boolean rc = _xdr.isComplete();
		_log.log(Level.FINEST, "hasNextMessage " + rc);
		return rc;
	}

	/**
	 *
	 * @see com.sun.grizzly.ProtocolParser#startBuffer(java.nio.ByteBuffer buffer)
	 */
	@Override
	public void startBuffer(ByteBuffer buffer) {
		_log.log(Level.FINEST, "startBuffer");
		_buffer = buffer;
		_buffer.flip();
	}

	/**
	 *
	 * @see com.sun.grizzly.ProtocolParser#releaseBuffer()
	 */
	@Override
	public boolean releaseBuffer() {
		_log.log(Level.FINEST,"releaseBuffer");
        if (_buffer != null) {
            _buffer.compact();
            _buffer.clear();
            _buffer = null;
        }

        return false;
	}
}
