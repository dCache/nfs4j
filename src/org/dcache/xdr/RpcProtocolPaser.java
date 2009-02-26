package org.dcache.xdr;

import com.sun.grizzly.ProtocolParser;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.logging.Logger;
import java.util.logging.Level;

public class RpcProtocolPaser implements ProtocolParser<Xdr> {

    private final static Logger _log = Logger.getLogger(RpcProtocolPaser.class.getName());
    private final static int MAX_XDR_SIZE = 128 * 1024;
    /**
     * Xdr which we try to construct.
     */
    private Xdr _xdr = null;
    private boolean _lastFragment = false;
    private int _fragmentToRead = 0;
    private boolean _isMuti = false;
    ByteBuffer _buffer;

    public RpcProtocolPaser() {
        _log.log(Level.FINEST, "new instance created");
    }

    /**
     *
     * @see com.sun.grizzly.ProtocolParser#isExpectingMoreData()
     */
    @Override
    public boolean isExpectingMoreData() {
        _log.log(Level.FINEST, "enter: isExpectingMoreData");
        boolean rc = _xdr != null && !(_fragmentToRead == 0 && _lastFragment);
        _log.log(Level.FINEST, "left: isExpectingMoreData {0}", rc);
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
        _log.log(Level.FINEST, "left: hasMoreBytesToParse {0}", rc);
        return rc;
    }

    /**
     *
     * @see com.sun.grizzly.ProtocolParser#getNextMessage()
     */
    @Override
    public Xdr getNextMessage() {
        _lastFragment = false;
        _fragmentToRead = 0;
        _isMuti = false;
        Xdr xdr = _xdr;
        _xdr = null;
        return xdr;
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

        /*
         * It may happent that single buffer will contain multiple fragments.
         * Loop over buffer contetn till we get complete message or buffer
         * has no more data.
         */
        while ( _buffer.hasRemaining() ) {

            if(_fragmentToRead <= 0 ) {

                if(_xdr == null )
                    _xdr = new Xdr(MAX_XDR_SIZE);
                
                _fragmentToRead = _buffer.getInt();
                if ((_fragmentToRead & 0x80000000) != 0) {
                    _fragmentToRead &= 0x7FFFFFFF;
                    _lastFragment = true;
                    if (_isMuti) {
                        _log.log(Level.INFO, "Multifragment XDR END");
                    }
                } else {
                    _lastFragment = false;
                    _isMuti = true;
                    _log.log(Level.INFO, "Multifragment XDR, expected len {0}, available {1}",
                        new Object[]{_fragmentToRead, _buffer.remaining()});
                }
            }

            /*
             * keep the origial buffer as is
             */
            ByteBuffer bytesToFlush = _buffer.duplicate();
            int n = _fragmentToRead > bytesToFlush.remaining() ? bytesToFlush.remaining() : _fragmentToRead;

            bytesToFlush.limit(bytesToFlush.position() + n);
            _xdr.fill(bytesToFlush);

            /*
             * update position in original buffer
             */
            _buffer.position(bytesToFlush.position());

            _fragmentToRead -= n;
            if(_fragmentToRead == 0 && _lastFragment) break;
        }
        
        if (_isMuti) {
            _log.log(Level.INFO, "Multifragment XDR, remaining {0} last: {1}",
                new Object[] {_fragmentToRead, _lastFragment} );
        }

        boolean rc = _fragmentToRead == 0 && _lastFragment;
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
        _buffer.order(ByteOrder.BIG_ENDIAN);
    }

    /**
     *
     * @see com.sun.grizzly.ProtocolParser#releaseBuffer()
     */
    @Override
    public boolean releaseBuffer() {
        _log.log(Level.FINEST, "releaseBuffer");
        if (_buffer != null) {
            _buffer.compact();
            _buffer = null;
        }

        return false;
    }
}
