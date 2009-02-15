package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rpc {

	private final static Logger _log = Logger.getLogger(Rpc.class.getName());
	private final ByteBuffer _buffer;

	private int _xid;
	private int _msgType;

    /**
     * Build a new Xdr object with a buffer of given size
     *
     * @param size	of the buffer in bytes
     */
	public Rpc(int size) {
		_buffer = ByteBuffer.allocate(size);
		// internal java byte order is BIG ENDIAN
		_buffer.order(ByteOrder.BIG_ENDIAN);
	}


	void fill(ByteBuffer b) {

		_buffer.put(b);

	}


	boolean isComplete() {
		_log.log(Level.FINEST, "isComplete " + _buffer.remaining());		
		return !_buffer.hasRemaining();
	}

	RpcMsg getMessage() throws RpcException {
		_buffer.flip();
		_xid = _buffer.getInt();
		_msgType = _buffer.getInt() == 0 ? RpcMessageType.CALL : RpcMessageType.REPLY;

		_log.log(Level.FINEST, "msgType = " + _msgType);
		_log.log(Level.FINEST, "xid     = " + _xid);


		if(_msgType == RpcMessageType.CALL) {
				return new RpcCall(_xid, _buffer);
		}
		return null;
	}

	int xid() {
		return _xid;
	}

}
