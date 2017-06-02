package org.dcache.nfs.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcache.nfs.ExportFile;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v3.xdr.READDIRPLUS3args;
import org.dcache.nfs.v3.xdr.READDIRPLUS3res;
import org.dcache.nfs.v3.xdr.cookieverf3;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.DirectoryStream;
import org.dcache.nfs.vfs.FileHandle;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.testutils.AssertXdr;
import org.dcache.testutils.NfsV3Ops;
import org.dcache.testutils.RpcCallBuilder;
import org.dcache.xdr.RpcCall;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class NfsServerV3READDIRPLUS_3Test {
    private FileHandle dirHandle;
    private Inode dirInode;
    private Stat dirStat;
    private VirtualFileSystem vfs;
    private NfsServerV3 nfsServer;

    @Before
    public void setup() throws Exception {
        dirHandle = new FileHandle(0, 1, 0, new byte[] { 0, 0, 0, 1 }); // the dir we want to read
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
        vfs = Mockito.mock(VirtualFileSystem.class); // the vfs serving it
        Mockito.when(vfs.getattr(Mockito.eq(dirInode))).thenReturn(dirStat);
        ExportFile exportFile = new ExportFile(this.getClass().getResource("simpleExports").toURI()); // same package as us
        nfsServer = new NfsServerV3(exportFile, vfs);
    }

    @Test
    public void testReadDirWithNoResults() throws Exception {

        byte[] cookieVerifier = cookieverf3.valueOf(0).value;

        // vfs will return an empty list from the vfs for dir (technically legal)
        Mockito.when(vfs.list(eq(dirInode), anyObject(), anyLong())).thenReturn(new DirectoryStream(cookieVerifier, Collections.emptyList()));

        // set up and execute the call
        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIRPLUS3args args = NfsV3Ops.readDirPlus(dirHandle);
        READDIRPLUS3res result = nfsServer.NFSPROC3_READDIRPLUS_3(call, args);

        Assert.assertEquals(nfsstat.NFS_OK, result.status);
        Assert.assertNull(result.resok.reply.entries); //no entries
        Assert.assertTrue(result.resok.reply.eof); //eof
        AssertXdr.assertXdrEncodable(result);
    }

    @Test
    public void testReadDirWithTinyLimit() throws Exception {

        byte[] cookieVerifier = cookieverf3.valueOf(0).value;

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat, 1));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat, 2));
        Mockito.when(vfs.list(eq(dirInode), anyObject(), anyLong())).thenReturn(new DirectoryStream(cookieVerifier, dirContents));

        // set up and execute the 1st call - no cookie, but very tight size limit
        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIRPLUS3args args = NfsV3Ops.readDirPlus(dirHandle, 10); //10 bytes - not enough for anything
        READDIRPLUS3res result = nfsServer.NFSPROC3_READDIRPLUS_3(call, args);

        Assert.assertEquals(nfsstat.NFSERR_TOOSMALL, result.status); //error response
    }

    @Test
    public void testContinueReadingAfterEOF() throws Exception {

        byte[] cookieVerifier = cookieverf3.valueOf(dirStat.getGeneration()).value;

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat, 1));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat, 2));
        Mockito.when(vfs.list(eq(dirInode), anyObject(), anyLong())).thenReturn(new DirectoryStream(cookieVerifier, dirContents));

        // set up and execute the 1st call - no cookie, but very tight size limit
        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIRPLUS3args args = NfsV3Ops.readDirPlus(dirHandle);
        READDIRPLUS3res result = nfsServer.NFSPROC3_READDIRPLUS_3(call, args);

        Assert.assertEquals(nfsstat.NFS_OK, result.status); //response ok
        Assert.assertTrue(result.resok.reply.eof); //eof
        AssertXdr.assertXdrEncodable(result);

        // re-read after EOF
        long cookie = result.resok.reply.entries.nextentry.cookie.value.value;
        cookieVerifier = result.resok.cookieverf.value;
        args = NfsV3Ops.readDirPlus(dirHandle, cookie, cookieVerifier);
        result = nfsServer.NFSPROC3_READDIRPLUS_3(call, args);

        Assert.assertEquals(nfsstat.NFS_OK, result.status); //response ok
        Assert.assertTrue(result.resok.reply.eof); //eof
        AssertXdr.assertXdrEncodable(result);
    }

    @Test
    public void testSemiValidVerifier() throws Exception {

        byte[] cookieVerifier = cookieverf3.valueOf(dirStat.getGeneration()).value;

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat, 1));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat, 2));
        dirContents.add(new DirectoryEntry("file", dirInode, dirStat, 3));
        Mockito.when(vfs.list(eq(dirInode), anyObject(), anyLong())).thenReturn(new DirectoryStream(cookieVerifier, dirContents));

        long cookie = 1;

        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIRPLUS3args args = NfsV3Ops.readDirPlus(dirHandle, cookie, cookieVerifier);
        READDIRPLUS3res result = nfsServer.NFSPROC3_READDIRPLUS_3(call, args);

        Assert.assertEquals(nfsstat.NFS_OK, result.status); //error response
        AssertXdr.assertXdrEncodable(result);
    }
}
