package org.dcache.door;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.grizzly.ProtocolParser;

public class AsciiCommandProtocolParser implements ProtocolParser<String> {

    private ByteBuffer _buffer;
    private ByteBuffer _messageBuffer = ByteBuffer.allocate(8192);

    private final static Logger _log = Logger.getLogger(AsciiCommandProtocolParser.class.getName());

    protected CharsetDecoder _asciiDecoder = Charset.forName("ISO-8859-1").newDecoder();
    
    /**
     * do we have complete message
     */
    private boolean _eom = false;

    /**
     *
     * @see com.sun.grizzly.ProtocolParser#getNextMessage()
     */
    @Override
    public String getNextMessage() {
        _log.log(Level.FINEST, "getNextMessage");

        ByteBuffer tmp = _messageBuffer.duplicate();
        String dcapRequest = null;
        try {
            tmp.flip();
            dcapRequest = _asciiDecoder.decode(tmp).toString();
            _messageBuffer.flip();
        } catch (CharacterCodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dcapRequest;
    }

    /**
     *
     * @see com.sun.grizzly.ProtocolParser#hasMoreBytesToParse()
     */
    @Override
    public boolean hasMoreBytesToParse() {
        boolean rc = _buffer != null && _buffer.hasRemaining();
        _log.log(Level.FINEST,"hasMoreBytesToParse " + rc);
        return rc;
    }

    /**
     * 
     * @see com.sun.grizzly.ProtocolParser#hasNextMessage()
     */
    @Override
    public boolean hasNextMessage() {
        if( _buffer == null ) {
            _log.log(Level.FINEST,"hasNextMessage false");
            return false;
        }

        while( !_eom && _messageBuffer.remaining() > 0 && _buffer.hasRemaining()) {
            byte b = _buffer.get() ;
            if( b == '\n' ) {
               _eom = true;
            }
             _messageBuffer.put(b);
        }

        _log.log(Level.FINEST,"hasNextMessage " + _eom);
        return _eom;
    }

    /**
     * 
     * @see com.sun.grizzly.ProtocolParser#isExpectingMoreData()
     */
    @Override
    public boolean isExpectingMoreData() {
        boolean rc =  !_eom && _messageBuffer.position() > 0;
        _log.log(Level.FINEST,"isExpectingMoreData " + rc);
        return rc;
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

    /**
     * 
     * @see com.sun.grizzly.ProtocolParser#startBuffer(java.nio.ByteBuffer buffer)
     */
    @Override
    public void startBuffer(ByteBuffer buffer) {
        _log.log(Level.FINEST,"startBuffer");
        _buffer = buffer;
        _buffer.flip();
        _eom = false;
    }

}
