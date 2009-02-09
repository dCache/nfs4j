package org.dcache.door;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import com.sun.grizzly.ProtocolParser;

public class AsciiCommandProtocolParser implements ProtocolParser<String> {

    private ByteBuffer _buffer;
    private ByteBuffer _messageBuffer = ByteBuffer.allocate(8192);


    protected CharsetDecoder _asciiDecoder = Charset.forName("ISO-8859-1").newDecoder();
    /**
     * did message complete
     */
    private boolean _eom = false;

    @Override
    public String getNextMessage() {
        System.out.println("getNextMessage");

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

    @Override
    public boolean hasMoreBytesToParse() {
        boolean rc = _buffer != null && _buffer.hasRemaining();
        System.out.println("hasMoreBytesToParse " + rc);
        return rc;
    }

    @Override
    public boolean hasNextMessage() {
        if( _buffer == null ) {
            System.out.println("hasNextMessage false");
            return false;
        }

        while( !_eom && _messageBuffer.remaining() > 0 && _buffer.hasRemaining()) {
            byte b = _buffer.get() ;
            if( b == '\n' ) {
               _eom = true;
            }
             _messageBuffer.put(b);
        }

        System.out.println("hasNextMessage " + _eom);
        return _eom;
    }

    @Override
    public boolean isExpectingMoreData() {
        boolean rc =  !_eom && _messageBuffer.position() > 0;
        System.out.println("isExpectingMoreData " + rc);
        return rc;
    }

    @Override
    public boolean releaseBuffer() {
        System.out.println("releaseBuffer");
        if (_buffer != null) {
            _buffer.compact();
            _buffer.clear();
            _buffer = null;
        }

        return false;
    }

    @Override
    public void startBuffer(ByteBuffer buffer) {
        System.out.println("startBuffer");
        _buffer = buffer;
        _buffer.flip();
        _eom = false;
    }

}
