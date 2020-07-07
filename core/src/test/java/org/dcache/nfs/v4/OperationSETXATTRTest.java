package org.dcache.nfs.v4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.setxattr_option4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Test;

import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OperationSETXATTRTest {

    private VirtualFileSystem vfs;
    private final Inode inode = Inode.forFile(new byte[]{1, 2, 3, 4});
    private final nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());

    @Before
    public void setUp() throws IOException {
        vfs = mock(VirtualFileSystem.class);
        Stat fileStat = new Stat();

        fileStat.setMode(Stat.S_IFREG | 0755);
        fileStat.setMTime(System.currentTimeMillis());
        fileStat.setATime(System.currentTimeMillis());
        fileStat.setCTime(System.currentTimeMillis());
        fileStat.setGeneration(1);
        fileStat.setNlink(2);
        fileStat.setUid(1);
        fileStat.setGid(2);
        fileStat.setDev(1);
        fileStat.setFileid(1);
        fileStat.setSize(512);

        when(vfs.getattr(any())).thenReturn(fileStat);
    }

    @Test
    public void testPropageteSetXattrModeEither() throws IOException {

        String key = "xattr1";
        byte[] expectedValue = "value1".getBytes(StandardCharsets.UTF_8);

        COMPOUND4args setxattrArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withSetXattr(key, expectedValue, setxattr_option4.SETXATTR4_EITHER)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        execute(context, setxattrArgs);
        verify(vfs).setXattr(inode, key, expectedValue, VirtualFileSystem.SetXattrMode.EITHER);

    }

    @Test
    public void testPropageteSetXattrModeReplace() throws IOException {

        String key = "xattr1";
        byte[] expectedValue = "value1".getBytes(StandardCharsets.UTF_8);

        COMPOUND4args setxattrArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withSetXattr(key, expectedValue, setxattr_option4.SETXATTR4_REPLACE)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        execute(context, setxattrArgs);
        verify(vfs).setXattr(inode, key, expectedValue, VirtualFileSystem.SetXattrMode.REPLACE);

    }

    @Test
    public void testPropageteSetXattrModeCreate() throws IOException {

        String key = "xattr1";
        byte[] expectedValue = "value1".getBytes(StandardCharsets.UTF_8);

        COMPOUND4args setxattrArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withSetXattr(key, expectedValue, setxattr_option4.SETXATTR4_CREATE)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        execute(context, setxattrArgs);
        verify(vfs).setXattr(inode, key, expectedValue, VirtualFileSystem.SetXattrMode.CREATE);

    }

    @Test(expected = BadXdrException.class)
    public void testPropageteSetXattrModeInval() throws IOException {

        String key = "xattr1";
        byte[] expectedValue = "value1".getBytes(StandardCharsets.UTF_8);

        COMPOUND4args setxattrArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withSetXattr(key, expectedValue, 7)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        execute(context, setxattrArgs);

    }

}
