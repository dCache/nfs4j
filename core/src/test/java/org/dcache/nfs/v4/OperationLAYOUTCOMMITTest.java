package org.dcache.nfs.v4;

import java.nio.charset.StandardCharsets;
import java.util.OptionalLong;
import org.dcache.nfs.status.BadLayoutException;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.vfs.Inode;
import org.junit.Before;
import org.junit.Test;

import org.dcache.nfs.v4.client.CompoundBuilder;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.VirtualFileSystem;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;
import static org.mockito.Mockito.mock;

public class OperationLAYOUTCOMMITTest {

    private final Inode inode = Inode.forFile(new byte[]{1, 2, 3, 4});
    private final nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());

    private NFSv4StateHandler sh;
    private VirtualFileSystem vfs;
    private NFSv41DeviceManager dm;

    @Before
    public void setUp() {
        sh = new NFSv4StateHandler();
        vfs = mock(VirtualFileSystem.class);
        dm = mock(NFSv41DeviceManager.class);
    }

    @Test(expected = BadLayoutException.class)
    public void testCommitOnReadLayout() throws Exception {

        NFS4Client client = createClient(sh);
        NFSv41Session session = client.createSession(1, 8196, 8192, 128, 16);

        StateOwner stateOwner = client.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        stateid4 openState = sh.getFileTracker().addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ, 0);

        COMPOUND4args layoutCommit = new CompoundBuilder()
                .withSequence(false, session.id(), 0, 0, 1)
                .withPutfh(fh)
                .withLayoutcommit(0, 100, true, openState, OptionalLong.of(100), layouttype4.LAYOUT4_FLEX_FILES, new byte[0])
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withMinorversion(1)
                .withDeviceManager(dm)
                .withCall(generateRpcCall())
                .build();

        execute(context, layoutCommit);
    }
}
