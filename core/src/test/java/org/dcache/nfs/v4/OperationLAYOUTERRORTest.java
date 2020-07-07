package org.dcache.nfs.v4;

import java.io.IOException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.device_error4;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Test;
import org.junit.Before;

import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class OperationLAYOUTERRORTest {

    private NFSv4StateHandler sh;
    private VirtualFileSystem vfs;
    private NFSv41DeviceManager dm;

    @Before
    public void setUp() throws Exception {
        sh = new NFSv4StateHandler();
        vfs = mock(VirtualFileSystem.class);
        dm = mock(NFSv41DeviceManager.class);
    }

    @Test(expected = NotSuppException.class)
    public void testNoPnfsConfigured() throws IOException {

        COMPOUND4args layoutError = new CompoundBuilder()
                .withLayoutError(0, 100, Stateids.OneStateId(), new device_error4[]{})
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, layoutError);
    }

    @Test
    public void testStatsPropagation() throws IOException {

        COMPOUND4args layoutError = new CompoundBuilder()
                .withLayoutError(0, 100, Stateids.OneStateId(), new device_error4[]{})
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withDeviceManager(dm)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, layoutError);

        verify(dm).layoutError(any(), any());
    }

}
