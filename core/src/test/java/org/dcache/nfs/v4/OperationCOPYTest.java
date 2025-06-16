package org.dcache.nfs.v4;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.OffloadNoReqsException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Test;

public class OperationCOPYTest {

    private NFSv4StateHandler stateHandler;
    private NFSv41Session session;
    private NFS4Client client;
    private VirtualFileSystem vfs;

    private Inode srcInode = Inode.forFile(new byte[] {1, 2, 3, 4});
    private Inode destInode = Inode.forFile(new byte[] {5, 6, 7, 8});
    private nfs_fh4 fhSrc = new nfs_fh4(srcInode.toNfsHandle());
    private nfs_fh4 fhDest = new nfs_fh4(destInode.toNfsHandle());

    private stateid4 srcStateid;
    private stateid4 destStateid;

    @Before
    public void setUp() throws UnknownHostException, ChimeraNFSException {
        stateHandler = new NFSv4StateHandler();
        client = createClient(stateHandler);
        session = client.createSession(1, 8196, 8192, 128, 16);

        state_owner4 owner = new state_owner4();
        owner.clientid = client.getId();
        owner.owner = "tests".getBytes(StandardCharsets.UTF_8);

        StateOwner stateOwner = new StateOwner(owner, 1);

        srcStateid = stateHandler.getFileTracker().addOpen(client, stateOwner, srcInode,
                nfs4_prot.OPEN4_SHARE_ACCESS_READ, nfs4_prot.OPEN4_SHARE_DENY_NONE).openStateId();
        destStateid = stateHandler.getFileTracker().addOpen(client, stateOwner, destInode,
                nfs4_prot.OPEN4_SHARE_ACCESS_WRITE, nfs4_prot.OPEN4_SHARE_DENY_NONE).openStateId();

        vfs = mock(VirtualFileSystem.class);
        when(vfs.copyFileRange(any(), anyLong(), any(), anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(8192L));
    }

    @Test(expected = OffloadNoReqsException.class)
    public void testCopyRejectNonConsecutive() throws Exception {

        COMPOUND4args copyArgs = new CompoundBuilder()
                .withMinorversion(2)
                .withPutfh(fhSrc)
                .withSavefh()
                .withPutfh(fhDest)
                .withIntraServerCopy(srcStateid, destStateid, 0L, 0L, 0L, true, false)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withCall(generateRpcCall())
                .build();

        execute(context, copyArgs);
    }

    @Test(expected = OffloadNoReqsException.class)
    public void testCopyRejectSyncLargeBlock() throws Exception {

        COMPOUND4args copyArgs = new CompoundBuilder()
                .withMinorversion(2)
                .withPutfh(fhSrc)
                .withSavefh()
                .withPutfh(fhDest)
                .withIntraServerCopy(srcStateid, destStateid, 0L, 0L, NFSv4Defaults.NFS4_MAXIOBUFFERSIZE + 1L, true,
                        true)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withCall(generateRpcCall())
                .build();

        execute(context, copyArgs);
    }

    @Test
    public void testCopyAcceptSyncSmallBlock() throws Exception {

        COMPOUND4args copyArgs = new CompoundBuilder()
                .withMinorversion(2)
                .withPutfh(fhSrc)
                .withSavefh()
                .withPutfh(fhDest)
                .withIntraServerCopy(srcStateid, destStateid, 0L, 0L, NFSv4Defaults.NFS4_MAXIOBUFFERSIZE, false, true)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        execute(context, copyArgs);
    }

    @Test
    public void testCopyAcceptAsyncLargeBlock() throws Exception {

        COMPOUND4args copyArgs = new CompoundBuilder()
                .withMinorversion(2)
                .withPutfh(fhSrc)
                .withSavefh()
                .withPutfh(fhDest)
                .withIntraServerCopy(srcStateid, destStateid, 0L, 0L, NFSv4Defaults.NFS4_MAXIOBUFFERSIZE + 1L, false,
                        true)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withFs(vfs)
                .withCall(generateRpcCall())
                .build();

        execute(context, copyArgs);
    }
}
