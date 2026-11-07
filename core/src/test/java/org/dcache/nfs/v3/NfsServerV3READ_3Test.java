package org.dcache.nfs.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dcache.nfs.ExportFile;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v3.xdr.READ3res;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.testutils.NfsV3Ops;
import org.dcache.testutils.RpcCallBuilder;
import org.junit.Before;
import org.junit.Test;

public class NfsServerV3READ_3Test {

    private static final long FILE_SIZE = 100;

    private Inode fileInode;
    private Inode fileInodePseudoFS;
    private VirtualFileSystem vfs;
    private NfsServerV3 nfsServer;
    private RpcCall call;

    @Before
    public void setup() throws Exception {
        fileInode = Inode.forFile(new byte[] {0, 0, 0, 2});
        fileInodePseudoFS = new Inode(0, 1, 0, new byte[] {0, 0, 0, 2});
        Stat fileStat = new Stat();
        // noinspection OctalInteger
        fileStat.setMode(Stat.S_IFREG | 0644);
        fileStat.setMTime(System.currentTimeMillis());
        fileStat.setATime(System.currentTimeMillis());
        fileStat.setCTime(System.currentTimeMillis());
        fileStat.setGeneration(1);
        fileStat.setNlink(1);
        fileStat.setUid(1);
        fileStat.setGid(2);
        fileStat.setDev(1);
        fileStat.setIno(2);
        fileStat.setSize(FILE_SIZE);
        vfs = mock(VirtualFileSystem.class);
        when(vfs.getattr(eq(fileInode))).thenReturn(fileStat);
        when(vfs.read(eq(fileInode), any(byte[].class), anyLong(), anyInt())).thenAnswer(i -> {
            long offset = i.getArgument(2);
            int count = i.getArgument(3);
            return (int) Math.max(0, Math.min(count, FILE_SIZE - offset));
        });
        ExportFile exportFile = new ExportFile(this.getClass().getResource("simpleExports").toURI());
        nfsServer = new NfsServerV3(exportFile, vfs);
        call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
    }

    private READ3res read(long offset, int count) {
        READ3res result = nfsServer.NFSPROC3_READ_3(call, NfsV3Ops.read(fileInodePseudoFS, offset, count));
        assertEquals(nfsstat.NFS_OK, result.status);
        return result;
    }

    @Test
    public void testReadBeforeEndIsNotEof() {
        READ3res result = read(0, 10);
        assertEquals(10, result.resok.count.value.value);
        assertFalse(result.resok.eof);
    }

    @Test
    public void testReadUpToEndIsEof() {
        READ3res result = read(90, 50);
        assertEquals(10, result.resok.count.value.value);
        assertTrue(result.resok.eof);
    }

    @Test
    public void testReadPastEndIsEof() {
        READ3res result = read(FILE_SIZE + 10, 50);
        assertEquals(0, result.resok.count.value.value);
        assertTrue(result.resok.eof);
    }
}
