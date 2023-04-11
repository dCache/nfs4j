package org.dcache.nfs.v4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Optional;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.xdr.fattr4_fs_layout_types;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.oncrpc4j.rpc.RpcAuthTypeNone;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;

import org.junit.Test;

import static org.dcache.nfs.v4.xdr.layouttype4.LAYOUT4_BLOCK_VOLUME;
import static org.dcache.nfs.v4.xdr.layouttype4.LAYOUT4_OSD2_OBJECTS;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;

import static org.mockito.BDDMockito.given;

/**
 *
 */
public class OperationGETATTRTest {

    @Test
    public void testDefaultLayoutType() throws IOException, URISyntaxException {

        NFSv41DeviceManager dm = mock(NFSv41DeviceManager.class);
        RpcCall call = mock(RpcCall.class);
        RpcTransport transport = mock(RpcTransport.class);
        ExportFile exportFile = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports").toURI());

        given(dm.getLayoutTypes()).willReturn(EnumSet.of(LAYOUT4_OSD2_OBJECTS, LAYOUT4_BLOCK_VOLUME));
        given(transport.getRemoteSocketAddress()).willReturn(new InetSocketAddress("172.16.4.1", 0));

        given(call.getCredential()).willReturn(new RpcAuthTypeNone());
        given(call.getTransport()).willReturn(transport);

        CompoundContext context = new CompoundContextBuilder()
                .withDeviceManager(dm)
                .withCall(call)
                .withExportTable(exportFile)
                .build();

        Optional<fattr4_fs_layout_types> res = (Optional<fattr4_fs_layout_types>) OperationGETATTR.fattr2xdr(nfs4_prot.FATTR4_FS_LAYOUT_TYPES, null, null, null, context);

        assertArrayEquals("export without explicit layout type must return all configures layout types",
                new int[] {LAYOUT4_OSD2_OBJECTS.getValue(), LAYOUT4_BLOCK_VOLUME.getValue()}, res.get().value);

    }

}
