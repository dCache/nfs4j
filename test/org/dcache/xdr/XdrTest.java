
package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class XdrTest {

    private ByteBuffer _buffer;


    @Before
    public void setUp() {
        _buffer = ByteBuffer.allocate(1024);
        _buffer.order(ByteOrder.BIG_ENDIAN);
    }

    @Test
    public void testDecodeInt() {

        int value = 17;
        _buffer.putInt(value);

        Xdr xdr = new Xdr(_buffer);
        xdr.beginDecoding();

        assertTrue("Decode value incorrect", xdr.xdrDecodeInt() == 17);
    }

    @Test
    public void testEncodeDecodeOpaque() {

        byte[] data = "some random data".getBytes();
        XdrEncodingStream encoder = new Xdr(_buffer);
        encoder.beginEncoding();
        encoder.xdrEncodeDynamicOpaque(data);
        encoder.endEncoding();
       
        XdrDecodingStream decoder = new Xdr(_buffer);
        decoder.beginDecoding();
        // get xdr fragment mark
        decoder.xdrDecodeInt();
        byte[] decoded = decoder.xdrDecodeDynamicOpaque();

        assertTrue("encoded/decoded data do not match", Arrays.equals(data, decoded));

    }


    @Test
    public void testDecodeBooleanTrue() {

        _buffer.putInt(1);

        Xdr xdr = new Xdr(_buffer);
        xdr.beginDecoding();
        assertTrue("Decode value incorrect", xdr.xdrDecodeBoolean() );
    }

    @Test
    public void testDecodeBooleanFale() {

        _buffer.putInt(0);

        Xdr xdr = new Xdr(_buffer);
        xdr.beginDecoding();
        assertFalse("Decode value incorrect", xdr.xdrDecodeBoolean() );
    }


        @Test
    public void testEncodeDecodeString() {

        String original = "some random data";
        XdrEncodingStream encoder = new Xdr(_buffer);
        encoder.beginEncoding();
        encoder.xdrEncodeString(original);
        encoder.endEncoding();

        XdrDecodingStream decoder = new Xdr(_buffer);
        decoder.beginDecoding();
        // get xdr fragment mark
        decoder.xdrDecodeInt();
        String decoded = decoder.xdrDecodeString();

        assertEquals("encoded/decoded string do not match", original, decoded);

    }
}
