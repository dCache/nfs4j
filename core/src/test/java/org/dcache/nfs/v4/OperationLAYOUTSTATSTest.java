package org.dcache.nfs.v4;

import java.io.IOException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.io_info4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.layoutupdate4;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Test;
import org.junit.Before;

import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OperationLAYOUTSTATSTest {

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

        io_info4 ioInfo = new io_info4();
        ioInfo.ii_bytes = 0;
        ioInfo.ii_count = 0;

        deviceid4 deviceId = new deviceid4(new byte[] {0x1});
        layoutupdate4 update = new layoutupdate4();
        update.lou_type = layouttype4.LAYOUT4_BLOCK_VOLUME.getValue();
        update.lou_body = new byte[0];

        COMPOUND4args layoutStats = new CompoundBuilder()
                .withLayoutStats(0, 100, Stateids.OneStateId(), ioInfo, ioInfo, deviceId, update)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, layoutStats);
    }

    @Test
    public void testStatsPropagation() throws IOException {

        io_info4 ioInfo = new io_info4();
        ioInfo.ii_bytes = 0;
        ioInfo.ii_count = 0;

        deviceid4 deviceId = new deviceid4(new byte[]{0x1});
        layoutupdate4 update = new layoutupdate4();
        update.lou_type = layouttype4.LAYOUT4_BLOCK_VOLUME.getValue();
        update.lou_body = new byte[0];

        COMPOUND4args layoutStats = new CompoundBuilder()
                .withLayoutStats(0, 100, Stateids.OneStateId(), ioInfo, ioInfo, deviceId, update)
                .build();

        CompoundContext context = new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(vfs)
                .withDeviceManager(dm)
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();

        execute(context, layoutStats);
        verify(dm).layoutStats(any(), any());
    }

}
