package org.dcache.xdr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RpcProtocolPaserTest {


    private RpcProtocolPaser _rpcParser;

    @Before
    public void setUp() {
        _rpcParser = new RpcProtocolPaser();
    }

    /**
     * Test method for {@link org.dcache.xdr.RpcProtocolPaser#isExpectingMoreData()}.
     */
    @Test
    public void testIsExpectingMoreDataNoData() {
        assertFalse("parser without data can't have a partial message", _rpcParser.isExpectingMoreData() );
    }

    /**
     * Test method for {@link org.dcache.xdr.RpcProtocolPaser#hasMoreBytesToParse()}.
     */
    @Test
    public void testHasMoreBytesToParseNoData() {
        assertFalse("parser without data can't have a part of a message", _rpcParser.hasMoreBytesToParse());
    }


    /**
     * Test method for {@link org.dcache.xdr.RpcProtocolPaser#hasNextMessage()}.
     */
    @Test
    public void testHasNextMessageNoData() {
        assertFalse("parser without any date can't have a message", _rpcParser.hasNextMessage());
    }


    @Test
    public void testFullFragment() {

        Xdr xdr = new Xdr(128);
        xdr.beginEncoding();
        xdr.xdrEncodeInt(1);
        xdr.endEncoding();

        ByteBuffer data = xdr.body();
        /*
         * do not flip here.
         * jut set current position to the limit;
         */
        data.position( data.limit() );
        _rpcParser.startBuffer(data);
        assertTrue("Complete message not detected", _rpcParser.hasNextMessage() );

    }

    @Test
    public void testPartialFragment() {

        ByteBuffer data = ByteBuffer.allocate(128);
        data.order(ByteOrder.BIG_ENDIAN);
        int messageLen = 27;

        data.putInt( messageLen | 0x80000000) ;

        _rpcParser.startBuffer(data);
        assertFalse("Partial message not detected", _rpcParser.hasNextMessage() );

    }

    @Test
    public void testPartialMultileFragment() {

        ByteBuffer data = ByteBuffer.allocate(128);
        data.order(ByteOrder.BIG_ENDIAN);
        int messageLen = 4;

        data.putInt( messageLen ) ;
        data.putInt(1);

        _rpcParser.startBuffer(data);
        assertFalse("Multiple fragment message not detected", _rpcParser.hasNextMessage() );
        assertTrue("Partial message do not request the remaining fragments", _rpcParser.isExpectingMoreData() );
    }

    @Test
    public void testFullMultileFragmentInOneBuffer() {

        ByteBuffer data = ByteBuffer.allocate(128);
        data.order(ByteOrder.BIG_ENDIAN);
        int messageLen = 4;

        data.putInt( messageLen ) ;
        data.putInt(1);

        data.putInt( messageLen | 0x80000000) ;
        data.putInt(1);

        _rpcParser.startBuffer(data);
        assertTrue("Multiple fragment message not detected", _rpcParser.hasNextMessage() );

    }

    @Test
    public void testFullMultileFragmentInTwoBuffer() {

        ByteBuffer fragment1 = ByteBuffer.allocate(128);
        ByteBuffer fragment2 = ByteBuffer.allocate(128);
        fragment1.order(ByteOrder.BIG_ENDIAN);
        fragment2.order(ByteOrder.BIG_ENDIAN);
        int messageLen = 4;

        fragment1.putInt( messageLen ) ;
        fragment1.putInt(1);

        _rpcParser.startBuffer(fragment1);
        assertFalse("Partial multi fragment message not detected", _rpcParser.hasNextMessage() );
        _rpcParser.releaseBuffer();

        fragment2.putInt( messageLen | 0x80000000) ;
        fragment2.putInt(1);

        _rpcParser.startBuffer(fragment2);
        assertTrue("Multiple fragment message not detected", _rpcParser.hasNextMessage() );
    }

    @Test
    public void testFullMultileInOneBuffer() {

        ByteBuffer data = ByteBuffer.allocate(128);
        data.order(ByteOrder.BIG_ENDIAN);
        int messageLen = 4;

        data.putInt( messageLen | 0x80000000) ;
        data.putInt(1);
        data.putInt( messageLen | 0x80000000) ;
        data.putInt(1);

        _rpcParser.startBuffer(data);
        assertTrue("First complete message not detected", _rpcParser.hasNextMessage() );
        assertTrue("Remaining data not detected", _rpcParser.hasMoreBytesToParse() );
        assertTrue("Second complete message not detected", _rpcParser.hasNextMessage() );

    }


}
