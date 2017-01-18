package org.dcache.nfs.v4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dcache.nfs.status.NotSameException;
import org.dcache.nfs.status.TooSmallException;
import org.dcache.nfs.v4.client.ReaddirStub;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.FileHandle;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.utils.Bytes;
import org.junit.Test;
import org.mockito.Mockito;
import org.junit.Before;

import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;

public class OperationREADDIRTest {

    private FileHandle dirHandle;
    private Inode dirInode;
    private Stat dirStat;
    private VirtualFileSystem vfs;
    private nfs_resop4 result;
    private CompoundContext context;

    @Before
    public void setup() throws Exception {
        dirHandle = new FileHandle(0, 1, 0, new byte[]{0, 0, 0, 1}); // the dir we want to read
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

        result = nfs_resop4.resopFor(nfs_opnum4.OP_READDIR);
        context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();
        context.currentInode(dirInode);
    }

    @Test
    public void testReadDirWithNoResults() throws Exception {
        // vfs will return an empty list from the vfs for dir (technically legal)
        Mockito.when(vfs.list(Mockito.eq(new Inode(dirHandle)))).thenReturn(Collections.<DirectoryEntry>emptyList());

        nfs_argop4 op = ReaddirStub.generateRequest(0, new verifier4( new byte[0]), 1024, 512);

        OperationREADDIR readdirOp = new OperationREADDIR(op);
        readdirOp.process(context, result);
    }

    @Test(expected = TooSmallException.class)
    public void testReadDirWithTinyLimit() throws Exception {
        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat));
        Mockito.when(vfs.list(Mockito.eq(dirInode))).thenReturn(dirContents);

        nfs_argop4 op = ReaddirStub.generateRequest(0, new verifier4(new byte[0]), 1024, 10);

        OperationREADDIR readdirOp = new OperationREADDIR(op);
        readdirOp.process(context, result);
    }

    @Test
    public void testSemiValidVerifier() throws Exception {

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("file", dirInode, dirStat));
        Mockito.when(vfs.list(Mockito.eq(dirInode))).thenReturn(dirContents);

        long cookie = 4; // 3 is the first allowed non zero value
        byte[] cookieVerifier = generateDirectoryVerifier(dirStat.getGeneration());

        nfs_argop4 op = ReaddirStub.generateRequest(cookie, new verifier4(cookieVerifier), 1024, 512);

        OperationREADDIR readdirOp = new OperationREADDIR(op);
        readdirOp.process(context, result);
    }

    @Test(expected = NotSameException.class)
    public void testInValidVerifier() throws Exception {

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("file", dirInode, dirStat));
        Mockito.when(vfs.list(Mockito.eq(dirInode))).thenReturn(dirContents);

        long cookie = 4; // 3 is the first allowed non zero value
        byte[] cookieVerifier = generateDirectoryVerifier(dirStat.getGeneration() - 1);

        nfs_argop4 op = ReaddirStub.generateRequest(cookie, new verifier4(cookieVerifier), 1024, 512);

        OperationREADDIR readdirOp = new OperationREADDIR(op);
        readdirOp.process(context, result);
    }

    private byte[] generateDirectoryVerifier(long generation) throws IllegalArgumentException {
        byte[] verifier = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];
        Bytes.putLong(verifier, 0, generation);
        return verifier;
    }

}
