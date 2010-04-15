package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.dcache.chimera.nfs.v4.xdr.layoutiomode4;
import org.dcache.chimera.nfs.v4.xdr.nfs_cookie4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.device_addr4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.deviceid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.GETDEVICELIST4res;
import org.dcache.chimera.nfs.v4.xdr.GETDEVICELIST4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.List;

import org.apache.log4j.Logger;

public class OperationGETDEVICELIST extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationGETDEVICELIST.class.getName());

    OperationGETDEVICELIST(nfs_argop4 args) {
    super(args, nfs_opnum4.OP_GETDEVICELIST);
    }

    @Override
    public boolean process(CompoundContext context) {

    GETDEVICELIST4res res = new GETDEVICELIST4res();

    try {

        /*
         * TODO: currently we redirect to ourself
         */

        /*
         * GETDEVICELIST This operation returns an array of items
         * (devlist_item4) that establish the association between the short
         * deviceid4 and the addressing information for that device, for a
         * particular layout type.
         */

        if (_args.opgetdevicelist.gdla_maxdevices.value.value < 0) {
        throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "negative maxcount");
        }

        if (_args.opgetdevicelist.gdla_maxdevices.value.value < 1) {
        throw new ChimeraNFSException(nfsstat4.NFS4ERR_TOOSMALL, "device list too small");
        }

        res.gdlr_resok4 = new GETDEVICELIST4resok();

        res.gdlr_resok4.gdlr_cookie = new nfs_cookie4(new uint64_t(1));
        res.gdlr_resok4.gdlr_cookieverf = new verifier4();
        res.gdlr_resok4.gdlr_cookieverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        /*
         * only deviceid==0 returned, which is the MDS ( current server )
         * all non regular inode IO done by MDS
         */

        DeviceID mdsID = DeviceID.valueOf(0);

        device_addr4 deviceAddr =
                DeviceManager.deviceAddrOf( context.getRpcCall().getTransport().getLocalSocketAddress() );

        NFS4IoDevice newDevice = new NFS4IoDevice(mdsID , deviceAddr);
        context.getDeviceManager().addIoDevice(newDevice, layoutiomode4.LAYOUTIOMODE4_ANY);

        List<NFS4IoDevice> deviceList = context.getDeviceManager().getIoDeviceList();

        int deviceListSize = deviceList.size();
        if (deviceListSize > _args.opgetdevicelist.gdla_maxdevices.value.value) {
            deviceListSize = _args.opgetdevicelist.gdla_maxdevices.value.value;
        }

        /*
         * FIXME: protect against empty list
         */
        res.gdlr_resok4.gdlr_deviceid_list = new deviceid4[deviceListSize];
        res.gdlr_resok4.gdlr_deviceid_list[0] = mdsID.toDeviceid4();

        for (int i = 0; i < deviceListSize; i++) {
            NFS4IoDevice device = deviceList.get(i);
            res.gdlr_resok4.gdlr_deviceid_list[i] = device.getDeviceId().toDeviceid4();
        }

        if (_log.isDebugEnabled()) {
        _log.debug("GETDEVICELIST4: new list of #" + res.gdlr_resok4.gdlr_deviceid_list.length + ", maxcount "
            + _args.opgetdevicelist.gdla_maxdevices.value.value);
        }

        /* we reply only one dummy entry. The rest is dynamic */
        res.gdlr_resok4.gdlr_eof = true;
        res.gdlr_status = nfsstat4.NFS4_OK;

    } catch (ChimeraNFSException he) {
        if (_log.isDebugEnabled()) {
        _log.debug("GETDEVICELIST4: " + he.getMessage());
        }
        res.gdlr_status = he.getStatus();
    } catch (Exception e) {
        _log.error("GETDEVICELIST4:", e);
        res.gdlr_status = nfsstat4.NFS4ERR_SERVERFAULT;
    }

    _result.opgetdevicelist = res;

        context.processedOperations().add(_result);
        return res.gdlr_status == nfsstat4.NFS4_OK;

    }
}
