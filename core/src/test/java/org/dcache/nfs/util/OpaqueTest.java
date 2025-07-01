package org.dcache.nfs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.nio.ByteBuffer;

import org.junit.Test;

public class OpaqueTest {
    @Test
    public void testMutableByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(64);
        buf.putInt(3, 0xAABBCCDD);
        buf.putInt(7, 0xEEFF0011);

        Opaque bufOpaque = Opaque.forMutableByteBuffer(buf, 3, 4);
        Opaque bytesOpaque = Opaque.forBytes(new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD});

        assertEquals(bufOpaque.toBase64(), bytesOpaque.toBase64());
        assertEquals(bufOpaque, bytesOpaque);
        assertEquals(bytesOpaque, bufOpaque);
        assertEquals(bytesOpaque.hashCode(), bufOpaque.hashCode());

        // unrelated changes should not affect equality
        buf.put(2, (byte) 0x7f);
        buf.putInt(7, 0);
        assertEquals(bufOpaque.toBase64(), bytesOpaque.toBase64());
        assertEquals(bufOpaque, bytesOpaque);
        assertEquals(bytesOpaque, bufOpaque);
        assertEquals(bytesOpaque.hashCode(), bufOpaque.hashCode());

        // change contents of mutable buffer
        buf.put(6, (byte) 0x12);
        assertNotEquals(bufOpaque.toBase64(), bytesOpaque.toBase64());
        assertNotEquals(bufOpaque, bytesOpaque);
        assertNotEquals(bytesOpaque, bufOpaque);
        assertNotEquals(bytesOpaque.hashCode(), bufOpaque.hashCode());
    }

    @Test
    public void testMutableByteArray() throws Exception {
        byte[] buf = new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04};
        Opaque bufOpaque = Opaque.forMutableByteArray(buf);
        Opaque bytesOpaque = Opaque.forBytes(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04});

        assertEquals(bufOpaque.toBase64(), bytesOpaque.toBase64());
        assertEquals(bufOpaque, bytesOpaque);
        assertEquals(bytesOpaque, bufOpaque);
        assertEquals(bytesOpaque.hashCode(), bufOpaque.hashCode());

        // change contents of mutable buffer
        buf[3] = (byte) 0xDD;
        assertNotEquals(bufOpaque.toBase64(), bytesOpaque.toBase64());
        assertNotEquals(bufOpaque, bytesOpaque);
        assertNotEquals(bytesOpaque, bufOpaque);
        assertNotEquals(bytesOpaque.hashCode(), bufOpaque.hashCode());
    }

    @Test
    public void testToImmutable() throws Exception {
        Opaque mutable = Opaque.forMutableByteBuffer(ByteBuffer.wrap(new byte[] {
                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04}), 0, 4);
        Opaque mutableToImmutable = mutable.toImmutableOpaque();
        assertEquals(mutable, mutableToImmutable);
        assertNotSame(mutable, mutableToImmutable);

        Opaque immutable = Opaque.forBytes(new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD});
        Opaque immutableToImmutable = immutable.toImmutableOpaque();
        assertEquals(immutable, immutableToImmutable);
        assertSame(immutable, immutableToImmutable);
    }
}
