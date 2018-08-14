package org.dcache.nfs.v4;

import com.google.common.primitives.Ints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.MovedException;
import org.dcache.nfs.status.TooSmallException;
import org.dcache.nfs.v4.xdr.entry4;
import org.dcache.nfs.v4.xdr.fattr4_rdattr_error;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.client.CompoundBuilder;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.DirectoryStream;
import org.dcache.nfs.vfs.FileHandle;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.oncrpc4j.xdr.Xdr;
import org.junit.Test;
import org.junit.Before;

import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OperationREADDIRTest {

    private FileHandle dirHandle;
    private Inode dirInode;
    private Stat dirStat;
    private VirtualFileSystem vfs;
    private nfs_resop4 result;
    private CompoundContext context;

    private int ino = 1; // first allowed cookie
    private long startCookie;
    private long verifier;
    private int maxCount = 1024;
    private int dirCount = 512;

    private entry4 entries;

    @Before
    public void setup() throws Exception {
        dirHandle = new FileHandle(0, 0, 0, new byte[]{0, 0, 0, 0}); // the dir we want to read
        dirInode = new Inode(dirHandle);
        dirStat = new Stat(); // the stat marking it as a dir
        //noinspection OctalInteger
        dirStat.setMode(Stat.S_IFDIR | 0755);
        dirStat.setMTime(System.currentTimeMillis());
        dirStat.setATime(System.currentTimeMillis());
        dirStat.setCTime(System.currentTimeMillis());
        dirStat.setGeneration(1);
        dirStat.setNlink(2);
        dirStat.setUid(1);
        dirStat.setGid(2);
        dirStat.setDev(1);
        dirStat.setFileid(1);
        dirStat.setSize(512);
        vfs = mock(VirtualFileSystem.class); // the vfs serving it
        when(vfs.getattr(eq(dirInode))).thenReturn(dirStat);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_READDIR);
        context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();
        context.currentInode(dirInode);
    }

    @Test
    public void testReadDirWithNoResults() throws Exception {
        givenDirectory();
        listed();
    }

    @Test(expected = TooSmallException.class)
    public void testReadDirWithTinyLimit() throws Exception {
        givenDirectory();
        withMaxRpcReplySize(10);
        listed();
    }

    @Test
    public void testSemiValidVerifier() throws Exception {

        givenDirectory(file("file"));
        long cookie = 4; // 3 is the first allowed non zero value
        fromCookie(cookie);
        listed();
    }

    @Test
    public void testEofIfLastEntryDoesNotFit() throws Exception {

        DirectoryEntry[] dirEntries = IntStream.range(0, 21)
                .mapToObj(i -> "file" + i)
                .map(this::file)
                .toArray(DirectoryEntry[]::new);

        givenDirectory(dirEntries);
        withMaxRpcReplySize(1000); // only 20 will fit

        listed();

        assertEquals("Not all entries returned", dirEntries.length - 1, entryCount());
        assertFalse("The last entry is missed", result.opreaddir.resok4.reply.eof);
    }

    @Test(expected = MovedException.class)
    public void testReaddirErrorAttributeFullOp() throws Exception {

        givenDirectory(file("file"));
        when(vfs.getAcl(any(Inode.class))).thenThrow(MovedException.class);

        listed(nfs4_prot.FATTR4_ACL);
    }

    @Test
    public void testReaddirErrorAttributeSingleEntry() throws Exception {

        givenDirectory(file("file"));
        when(vfs.getAcl(any(Inode.class))).thenThrow(new MovedException()); // can't use MovedException.class as status value not initialized

        listed(nfs4_prot.FATTR4_ACL, nfs4_prot.FATTR4_RDATTR_ERROR);

        fattr4_rdattr_error rderror = new fattr4_rdattr_error(new Xdr(entries.attrs.attr_vals.value));

        assertTrue("rdattr_error is not set", entries.attrs.attrmask.isSet(nfs4_prot.FATTR4_RDATTR_ERROR));
        assertEquals("Invalid error code returned", nfsstat.NFSERR_MOVED, rderror.value);
    }

    private DirectoryEntry dir(String name) {

        int cookie = ino++;

        FileHandle handle = new FileHandle(0, 1, 0, Ints.toByteArray(cookie));
        Inode inode = new Inode(handle);
        Stat stat = new Stat(); // the stat marking it as a dir
        //noinspection OctalInteger
        stat.setMode(Stat.S_IFDIR | 0755);
        stat.setMTime(System.currentTimeMillis());
        stat.setATime(System.currentTimeMillis());
        stat.setCTime(System.currentTimeMillis());
        stat.setGeneration(1);
        stat.setNlink(2);
        stat.setUid(1);
        stat.setGid(2);
        stat.setDev(1);
        stat.setFileid(cookie);
        stat.setSize(512);

        try {
            when(vfs.getattr(eq(inode))).thenReturn(stat);
        } catch (IOException e) {
            fail();
        }
        return new DirectoryEntry(name, inode, stat, cookie);
    }

    private DirectoryEntry file(String name) {

        int cookie = ino++;

        FileHandle handle = new FileHandle(0, 1, 0, Ints.toByteArray(cookie));
        Inode inode = new Inode(handle);
        Stat stat = new Stat(); // the stat marking it as a dir
        //noinspection OctalInteger
        stat.setMode(Stat.S_IFREG | 0644);
        stat.setMTime(System.currentTimeMillis());
        stat.setATime(System.currentTimeMillis());
        stat.setCTime(System.currentTimeMillis());
        stat.setGeneration(1);
        stat.setNlink(2);
        stat.setUid(1);
        stat.setGid(2);
        stat.setDev(1);
        stat.setFileid(cookie);
        stat.setSize(1024);

        try {
            when(vfs.getattr(eq(inode))).thenReturn(stat);
        } catch (IOException e) {
            fail();
        }
        return new DirectoryEntry(name, inode, stat, cookie);
    }

    private void givenDirectory(DirectoryEntry... enties) throws IOException {

          verifier4 cookieVerifier = verifier4.valueOf(1);

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(dir("."));
        dirContents.add(dir(".."));
        dirContents.addAll(Arrays.asList(enties));
        when(vfs.list(eq(dirInode), anyObject(), anyLong())).thenReturn(new DirectoryStream(cookieVerifier.value, dirContents));
    }

    public void fromCookie(long start) {
        startCookie = start;
    }

    public void withMaxDirReplySize(int size) {
        dirCount = size;
    }

    public void withMaxRpcReplySize(int size) {
        maxCount = size;
    }

    public void listed(int... attrs) throws IOException {
        nfs_argop4 op = new CompoundBuilder()
                .withReaddir(startCookie, verifier4.valueOf(verifier), dirCount, maxCount, attrs)
                .build().argarray[0];

        OperationREADDIR readdirOp = new OperationREADDIR(op);
        readdirOp.process(context, result);

        entries = result.opreaddir.resok4.reply.entries;
    }

    private int entryCount() {
        int n = 0;
        while (entries != null) {
            entries = entries.nextentry;
            n++;
        }
        return n;
    }
}
