package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.GETDEVICEINFO4args;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.deviceid4;
import org.dcache.chimera.nfs.v4.xdr.layouttype4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;

public class GetdeviceinfoStub {

    public static nfs_argop4 generateRequest(deviceid4 devId) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICEINFO;
        op.opgetdeviceinfo = new GETDEVICEINFO4args();

        op.opgetdeviceinfo.gdia_device_id = devId;
        op.opgetdeviceinfo.gdia_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;

        count4 cn = new count4();
        cn.value = new uint32_t(0xffffff);
        op.opgetdeviceinfo.gdia_maxcount = cn;
        op.opgetdeviceinfo.gdia_notify_types = new bitmap4();
        op.opgetdeviceinfo.gdia_notify_types.value = new uint32_t[1];
        op.opgetdeviceinfo.gdia_notify_types.value[0] = new uint32_t(0);

        return op;

    }
}
