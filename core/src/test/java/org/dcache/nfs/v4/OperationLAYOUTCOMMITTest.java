package org.dcache.nfs.v4;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_WRITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.OptionalLong;

import org.dcache.nfs.status.BadLayoutException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class OperationLAYOUTCOMMITTest {

    private final Inode inode = Inode.forFile(new byte[] {1, 2, 3, 4});
    private final nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());

    private NFSv4StateHandler sh;
    private VirtualFileSystem vfs;
    private NFSv41DeviceManager dm;
    private NFS4Client client;
    private NFSv41Session session;
    private StateOwner stateOwner;

    @Before
    public void setUp() throws Exception {
        sh = new NFSv4StateHandler();
        vfs = mock(VirtualFileSystem.class);
        dm = mock(NFSv41DeviceManager.class);
        client = createClient(sh);
        session = client.createSession(1, 8196, 8192, 128, 16);
        stateOwner = client.getOrCreateOwner("client".getBytes(StandardCharsets.UTF_8), new seqid4(0));

    }

    @Test(expected = BadLayoutException.class)
    @Ignore
    public void testCommitOnReadLayout() throws Exception {

        StateOwner stateOwner = client.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        var openRecord = sh.getFileTracker().addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ, 0);

        COMPOUND4args layoutCommit = new CompoundBuilder()
                .withSequence(false, session.id(), 0, 0, 1)
                .withPutfh(fh)
                .withLayoutcommit(0, 100, true, openRecord.openStateId(), OptionalLong.of(100),
                        layouttype4.LAYOUT4_FLEX_FILES, new byte[0])
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

    @Test
    public void testReturnNewSizeOnFileChange() throws Exception {

        when(dm.layoutCommit(any(), any())).thenReturn(OptionalLong.of(100));

        StateOwner stateOwner = client.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        var openRecord = sh.getFileTracker().addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        COMPOUND4args layoutCommit = new CompoundBuilder()
                .withSequence(false, session.id(), 0, 0, 1)
                .withPutfh(fh)
                .withLayoutcommit(0, 100, true, openRecord.openStateId(), OptionalLong.of(100),
                        layouttype4.LAYOUT4_FLEX_FILES, new byte[0])
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withMinorversion(1)
                .withDeviceManager(dm)
                .withCall(generateRpcCall())
                .build();

        COMPOUND4res res = execute(context, layoutCommit);
        assertTrue("new size is not set", res.resarray.get(2).oplayoutcommit.locr_resok4.locr_newsize.ns_sizechanged);

        assertEquals("invalid size returned", 100L, res.resarray.get(
                2).oplayoutcommit.locr_resok4.locr_newsize.ns_size.value);
    }

    @Test
    public void testNoNewSizeOnNoFileChange() throws Exception {

        when(dm.layoutCommit(any(), any())).thenReturn(OptionalLong.empty());

        StateOwner stateOwner = client.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        var openRecord = sh.getFileTracker().addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        COMPOUND4args layoutCommit = new CompoundBuilder()
                .withSequence(false, session.id(), 0, 0, 1)
                .withPutfh(fh)
                .withLayoutcommit(0, 100, true, openRecord.openStateId(), OptionalLong.of(100),
                        layouttype4.LAYOUT4_FLEX_FILES, new byte[0])
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withMinorversion(1)
                .withDeviceManager(dm)
                .withCall(generateRpcCall())
                .build();

        COMPOUND4res res = execute(context, layoutCommit);
        assertFalse("unexpected new size", res.resarray.get(2).oplayoutcommit.locr_resok4.locr_newsize.ns_sizechanged);
    }

    @Test(expected = NotSuppException.class)
    public void testNoPnfsConfigured() throws Exception {

        var openRecord = sh.getFileTracker().addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        COMPOUND4args layoutCommit = new CompoundBuilder()
                .withSequence(false, session.id(), 0, 0, 1)
                .withPutfh(fh)
                .withLayoutcommit(0, 100, true, openRecord.openStateId(), OptionalLong.of(100),
                        layouttype4.LAYOUT4_FLEX_FILES, new byte[0])
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, layoutCommit);
    }
}
