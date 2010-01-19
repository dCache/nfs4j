package org.dcache.chimera.nfs.v4.client;

import java.io.IOException;

import java.nio.ByteBuffer;
import org.dcache.chimera.nfs.v4.xdr.GETDEVICELIST4args;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.layouttype4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_cookie4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfsv4_1_file_layout_ds_addr4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrDecodingStream;

public class GetDeviceListStub {

    public static nfs_argop4 normal() {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICELIST;
        op.opgetdevicelist = new GETDEVICELIST4args();

        op.opgetdevicelist.gdla_cookie = new nfs_cookie4();
        op.opgetdevicelist.gdla_cookie.value = new uint64_t(0);

        op.opgetdevicelist.gdla_cookieverf = new verifier4();
        op.opgetdevicelist.gdla_cookieverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        op.opgetdevicelist.gdla_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;

        op.opgetdevicelist.gdla_maxdevices = new count4();
        op.opgetdevicelist.gdla_maxdevices.value = new uint32_t(256);

        return op;
    }

    public static nfsv4_1_file_layout_ds_addr4 decodeFileDevice(byte[] data)
            throws OncRpcException, IOException {
        XdrDecodingStream xdr = new Xdr(ByteBuffer.wrap(data));

        nfsv4_1_file_layout_ds_addr4 device = new nfsv4_1_file_layout_ds_addr4();

        xdr.beginDecoding();
        device.xdrDecode(xdr);
        xdr.endDecoding();

        return device;
    }

}
