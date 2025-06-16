package org.dcache.nfs.v4;

import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumSet;

import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.device_addr4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.junit.Test;

/**
 *
 */
public class OperationGETDEVICEINFOTest {

    @Test
    public void testNotificationBitmapSize() throws IOException, URISyntaxException {

        NFSv41DeviceManager dm = mock(NFSv41DeviceManager.class);
        given(dm.getDeviceInfo(any(), any())).willReturn(mock(device_addr4.class));

        given(dm.getLayoutTypes()).willReturn(EnumSet.allOf(layouttype4.class));
        CompoundContext context = new CompoundContextBuilder()
                .withDeviceManager(dm)
                .withCall(generateRpcCall())
                .build();

        COMPOUND4args gdiArgs = new CompoundBuilder()
                .withGetdeviceinfo(new deviceid4(new byte[] {0x7}), layouttype4.LAYOUT4_NFSV4_1_FILES)
                .build();

        COMPOUND4res res = execute(context, gdiArgs);
        assertEquals("invalid notification bitmap size", 1, res.resarray.get(
                0).opgetdeviceinfo.gdir_resok4.gdir_notification.value.length);
    }
}
