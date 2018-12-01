package org.dcache.nfs.v4;

import java.io.IOException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.v4.client.CompoundBuilder;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;
import org.junit.Test;
import org.junit.Before;

import static org.dcache.nfs.v4.NfsTestUtils.*;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.vfs.Inode;

public class OperationLOCKUTest {

    private NFSv4StateHandler stateHandler;
    private Inode inode = Inode.forFile(new byte[]{1, 2, 3, 4});
    private nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());

    @Before
    public void setUp() {
        stateHandler = new NFSv4StateHandler();
    }

    @Test(expected = InvalException.class)
    public void testOffsetLengthOverflow() throws IOException {

        COMPOUND4args lockuArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withLocku(nfs_lock_type4.READW_LT,
                        Stateids.OneStateId(),
                        0, Long.MAX_VALUE , Long.MIN_VALUE + 1) // treated as unsigned
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        execute(context, lockuArgs);

    }

}
