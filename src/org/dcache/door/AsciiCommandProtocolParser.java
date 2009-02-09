package org.dcache.door;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import com.sun.grizzly.ProtocolParser;

public class AsciiCommandProtocolParser implements ProtocolParser<String> {

    private ByteBuffer _buffer;


    protected CharsetDecoder _asciiDecoder = Charset.forName("ISO-8859-1").newDecoder();
    /**
     * did message complete
     */
    private boolean _eom = false;

    @Override
    public String getNextMessage() {

        ByteBuffer tmp = _buffer.duplicate();
        String dcapRequest = null;
        try {
            dcapRequest = _asciiDecoder.decode(tmp).toString();
        } catch (CharacterCodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dcapRequest;
    }

    @Override
    public boolean hasMoreBytesToParse() {
        return _eom && _buffer != null && _buffer.position() > 0;
    }

    @Override
    public boolean hasNextMessage() {

        if( _buffer == null ) {
            return false;
        }

        _eom = _buffer.get( _buffer.limit() -1 ) == '\n';

        return _eom;
    }

    @Override
    public boolean isExpectingMoreData() {
        return ! _eom;
    }

    @Override
    public boolean releaseBuffer() {
        if (_buffer != null) {
            _buffer.compact();
            _buffer.clear();
            _buffer = null;
        }

        _eom = false;

        return false;
    }

    @Override
    public void startBuffer(ByteBuffer buffer) {
        _buffer = buffer;
        _buffer.flip();
    }

}
