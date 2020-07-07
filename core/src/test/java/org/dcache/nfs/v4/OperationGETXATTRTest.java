package org.dcache.nfs.v4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Test;

import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OperationGETXATTRTest {

    private VirtualFileSystem vfs;
    private final Inode inode = Inode.forFile(new byte[]{1, 2, 3, 4});
    private final nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());

    @Before
    public void setUp() {
        vfs = mock(VirtualFileSystem.class);
    }

    @Test
    public void testPropageteGetXattr() throws IOException {

        String key = "xattr1";
        byte[] expectedValue = "value1".getBytes(StandardCharsets.UTF_8);

        COMPOUND4args getxattrArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withGetXattr(key)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        when(vfs.getXattr(inode, key)).thenReturn(expectedValue);

        COMPOUND4res res = execute(context, getxattrArgs);
        verify(vfs).getXattr(inode, key);
        assertArrayEquals(expectedValue, res.resarray.get(1).opgetxattr.gxr_value.value);

    }
}
