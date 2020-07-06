package org.dcache.nfs.v4;

import java.io.IOException;
import java.net.UnknownHostException;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v4.client.CompoundBuilder;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.vfs.Inode;
import org.junit.Test;
import org.junit.Before;

import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.dcache.nfs.v4.NfsTestUtils.*;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import org.dcache.nfs.v4.xdr.COMPOUND4res;

public class OperationWRITETest {

    private Inode inode = Inode.forFile(new byte[]{1, 2, 3, 4});
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
        fileStat.setFileid(1);
        fileStat.setSize(512);

        vfs = mock(VirtualFileSystem.class);
    }

    @Test
    public void testLeaseUpdateForV40Client() throws UnknownHostException, ChimeraNFSException, IOException {

        stateid4 stateid = mock(stateid4.class);
        NFSv4StateHandler stateHandler = mock(NFSv4StateHandler.class);

        when(vfs.getattr(any())).thenReturn(fileStat);
        when(vfs.write(any(), any(), anyLong(), any()))
                .thenReturn(new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, 1));

        COMPOUND4args writeArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withWrite(0, new byte[0], stateid)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withFs(vfs)
                .withMinorversion(0)
                .withCall(generateRpcCall())
                .build();

        execute(context, writeArgs);
        verify(stateHandler, times(1)).updateClientLeaseTime(stateid);
    }

    @Test
    public void testNoLeaseUpdateForV41Client() throws UnknownHostException, ChimeraNFSException, IOException {

        stateid4 stateid = mock(stateid4.class);
        NFSv4StateHandler stateHandler = mock(NFSv4StateHandler.class);

        when(vfs.getattr(any())).thenReturn(fileStat);
        when(vfs.write(any(), any(), anyLong(), any()))
                .thenReturn(new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, 1));

        COMPOUND4args writeArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withWrite(0, new byte[0], stateid)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withFs(vfs)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, writeArgs);
        verify(stateHandler, never()).updateClientLeaseTime(stateid);
    }

    @Test
    public void testReturnWriteVerifier() throws UnknownHostException, ChimeraNFSException, IOException {

        stateid4 stateid = mock(stateid4.class);
        NFSv4StateHandler stateHandler = mock(NFSv4StateHandler.class);
        verifier4 verifier = mock(verifier4.class);

        when(vfs.getattr(any())).thenReturn(fileStat);
        when(vfs.write(any(), any(), anyLong(), any()))
                .thenReturn(new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, 1));

        COMPOUND4args writeArgs = new CompoundBuilder()
                .withPutfh(fh)
                .withWrite(0, new byte[0], stateid)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withFs(vfs)
                .withMinorversion(1)
                .withRebootVerifier(verifier)
                .withCall(generateRpcCall())
                .build();

        COMPOUND4res res = execute(context, writeArgs);
        assertSame(verifier, res.resarray.get(1).opwrite.resok4.writeverf);
    }
}
