package org.dcache.nfs.v4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.dcache.nfs.v4.client.CompoundBuilder;
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

public class OperationLISTXATTRSTest {

    private VirtualFileSystem vfs;
    private final Inode inode = Inode.forFile(new byte[]{1, 2, 3, 4});
    private final nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());

    @Before
    public void setUp() {
        vfs = mock(VirtualFileSystem.class);
    }

    @Test
    public void testPropageteListXattrs() throws IOException {

        String[] expectedAttrs = {
            "xattr1", "xattr2", "xattr3"
        };

        COMPOUND4args listxattrsArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withListXattrs(0, 100)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        when(vfs.listXattrs(inode)).thenReturn(expectedAttrs);

        COMPOUND4res res = execute(context, listxattrsArgs);
        verify(vfs).listXattrs(inode);
        assertArrayEquals(expectedAttrs, res.resarray.get(1).oplistxattrs.lxr_value.lxr_names);

    }
}
