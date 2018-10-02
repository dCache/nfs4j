package org.dcache.nfs.vfs;

import com.google.common.primitives.Ints;
import java.util.NavigableSet;
import java.util.TreeSet;
import org.dcache.nfs.v4.xdr.verifier4;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 */
public class DirectoryStreamTest {

    private DirectoryStream stream;
    @Before
    public void setUp() {

        NavigableSet<DirectoryEntry> entries = new TreeSet<>();
        byte[] verifier = verifier4.valueOf(System.currentTimeMillis()).value;

        for (int i = 0; i < 10; i++) {
            FileHandle fh = new FileHandle(0, 0, 0, Ints.toByteArray(i));
            Inode inode = new Inode(fh);
            Stat stat = new Stat();

            stat.setMode(Stat.S_IFDIR | 0755);
            stat.setMTime(System.currentTimeMillis());
            stat.setATime(System.currentTimeMillis());
            stat.setCTime(System.currentTimeMillis());
            stat.setGeneration(1);
            stat.setNlink(2);
            stat.setUid(1);
            stat.setGid(2);
            stat.setDev(1);
            stat.setFileid(i);
            stat.setSize(512);

            entries.add(new DirectoryEntry(String.format("file-%d", i), inode, stat, i));
        }

        stream = new DirectoryStream(verifier, entries);
    }

    @Test
    public void testTail() {
        DirectoryStream tail = stream.tail(3);
        assertEquals(tail.iterator().next().getCookie(), 4);
        assertArrayEquals(stream.getVerifier(), tail.getVerifier());
    }

    @Test
    public void testTransform() {
        DirectoryStream transformed = stream.transform(d -> new DirectoryEntry(d.getName().toUpperCase(), d.getInode(), d.getStat(), d.getCookie()));
        assertArrayEquals(stream.getVerifier(), transformed.getVerifier());
        for (DirectoryEntry e : transformed) {
            assertTrue(e.getName().startsWith("FILE"));
        }
    }

    @Test
    public void testTransformAndTail() {

        DirectoryStream transformed = stream.transform(d -> new DirectoryEntry(d.getName().toUpperCase(), d.getInode(), d.getStat(), d.getCookie()));
        DirectoryStream tail = transformed.tail(3);
        DirectoryEntry next = tail.iterator().next();

        assertEquals(next.getCookie(), 4);
        assertEquals("FILE-4", next.getName());

    }

    @Test
    public void testMultipleTransformAndTail() {
        DirectoryStream transformed = stream.transform(d -> new DirectoryEntry(d.getName().toUpperCase(), d.getInode(), d.getStat(), d.getCookie()));
        DirectoryStream tail = transformed.tail(3).tail(5);
        DirectoryEntry next = tail.iterator().next();

        assertEquals(next.getCookie(), 6);
        assertEquals("FILE-6", next.getName());
    }

    @Test
    public void testMultipleTransform() {
        DirectoryStream transformed = stream
                .transform(d -> new DirectoryEntry(d.getName().toUpperCase(), d.getInode(), d.getStat(), d.getCookie()))
                .transform(d -> new DirectoryEntry("a" + d.getName(), d.getInode(), d.getStat(), d.getCookie()));

        assertArrayEquals(stream.getVerifier(), transformed.getVerifier());
        for (DirectoryEntry e : transformed) {
            assertTrue(e.getName().startsWith("aFILE"));
        }
    }
}
