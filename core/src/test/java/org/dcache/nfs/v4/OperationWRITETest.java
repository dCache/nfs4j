package org.dcache.nfs.v4;

import static org.dcache.nfs.v4.NfsTestUtils.*;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Test;

public class OperationWRITETest {

    private Inode inode = Inode.forFile(new byte[] {1, 2, 3, 4});
    private nfs_fh4 fh = new nfs_fh4(inode.toNfsHandle());
    private VirtualFileSystem vfs;
    private Stat fileStat;

    @Before
    public void setUp() {

        fileStat = new Stat();
        fileStat.setMode(Stat.S_IFREG | 0755);
        fileStat.setMTime(System.currentTimeMillis());
        fileStat.setATime(System.currentTimeMillis());
        fileStat.setCTime(System.currentTimeMillis());
        fileStat.setGeneration(1);
        fileStat.setNlink(2);
        fileStat.setUid(1);
        fileStat.setGid(2);
        fileStat.setDev(1);
        fileStat.setIno(1);
        fileStat.setSize(512);

        vfs = mock(VirtualFileSystem.class);
    }

    @Test
    public void testLeaseUpdateForV40Client() throws UnknownHostException, ChimeraNFSException, IOException {

        stateid4 stateid = generateStateId();
        NFSv4StateHandler stateHandler = mock(NFSv4StateHandler.class);
        NFS4Client client = mock(NFS4Client.class);
        NFSv41Session session = mock(NFSv41Session.class);
        FileTracker fileTracker = mock(FileTracker.class);

        when(stateHandler.getFileTracker()).thenReturn(fileTracker);
        when(fileTracker.getShareAccess(any(), any(), any())).thenReturn(nfs4_prot.OPEN4_SHARE_ACCESS_WRITE);
        when(stateHandler.getClientIdByStateId(any())).thenReturn(client);
        when(session.getClient()).thenReturn(client);
        when(stateHandler.getClientIdByStateId(any())).thenReturn(client);

        when(vfs.getattr(any())).thenReturn(fileStat);
        when(vfs.getattr(any(), any())).thenCallRealMethod();
        when(vfs.write(any(), any(), anyLong(), any()))
                .thenReturn(new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, 1));

        COMPOUND4args writeArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withWrite(0, new byte[0], stateid)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withFs(vfs)
                .withMinorversion(0)
                .withCall(generateRpcCall())
                .build();

        execute(context, writeArgs);
        verify(stateHandler, times(1)).updateClientLeaseTime(stateid);
    }

    @Test
    public void testNoLeaseUpdateForV41Client() throws UnknownHostException, ChimeraNFSException, IOException {

        stateid4 stateid = generateStateId();
        NFSv4StateHandler stateHandler = mock(NFSv4StateHandler.class);
        NFS4Client client = mock(NFS4Client.class);
        NFSv41Session session = mock(NFSv41Session.class);
        FileTracker fileTracker = mock(FileTracker.class);

        when(stateHandler.getFileTracker()).thenReturn(fileTracker);
        when(fileTracker.getShareAccess(any(), any(), any())).thenReturn(nfs4_prot.OPEN4_SHARE_ACCESS_WRITE);
        when(stateHandler.getClientIdByStateId(any())).thenReturn(client);
        when(session.getClient()).thenReturn(client);
        when(stateHandler.getClientIdByStateId(any())).thenReturn(client);

        when(vfs.getattr(any())).thenReturn(fileStat);
        when(vfs.getattr(any(), any())).thenCallRealMethod();
        when(vfs.write(any(), any(), anyLong(), any()))
                .thenReturn(new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, 1));

        COMPOUND4args writeArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withWrite(0, new byte[0], stateid)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withFs(vfs)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, writeArgs);
        verify(stateHandler, never()).updateClientLeaseTime(stateid);
    }

    @Test
    public void testReturnWriteVerifier() throws UnknownHostException, ChimeraNFSException, IOException {

        stateid4 stateid = generateStateId();
        NFSv4StateHandler stateHandler = mock(NFSv4StateHandler.class);
        NFS4Client client = mock(NFS4Client.class);
        NFSv41Session session = mock(NFSv41Session.class);
        FileTracker fileTracker = mock(FileTracker.class);

        when(stateHandler.getFileTracker()).thenReturn(fileTracker);
        when(fileTracker.getShareAccess(any(), any(), any())).thenReturn(nfs4_prot.OPEN4_SHARE_ACCESS_WRITE);
        when(stateHandler.getClientIdByStateId(any())).thenReturn(client);
        when(session.getClient()).thenReturn(client);
        when(stateHandler.getClientIdByStateId(any())).thenReturn(client);

        verifier4 verifier = mock(verifier4.class);

        when(vfs.getattr(any())).thenReturn(fileStat);
        when(vfs.getattr(any(), any())).thenCallRealMethod();
        when(vfs.write(any(), any(), anyLong(), any()))
                .thenReturn(new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, 1));

        COMPOUND4args writeArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withWrite(0, new byte[0], stateid)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withSession(session)
                .withFs(vfs)
                .withMinorversion(1)
                .withRebootVerifier(verifier)
                .withCall(generateRpcCall())
                .build();

        COMPOUND4res res = execute(context, writeArgs);
        assertSame(verifier, res.resarray.get(1).opwrite.resok4.writeverf);
    }
}
