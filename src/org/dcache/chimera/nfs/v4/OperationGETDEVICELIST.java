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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationGETDEVICELIST extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationGETDEVICELIST.class.getName());

    OperationGETDEVICELIST(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
    super(fs, exports, call$, fh, args, nfs_opnum4.OP_GETDEVICELIST);
    }

    @Override
    public NFSv4OperationResult process() {

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

        byte[] mdsID = id2deviceid(0);

        InetSocketAddress[] addresses = new InetSocketAddress[1];
        addresses[0] = _callInfo.getTransport().getLocalSocketAddress();

        device_addr4 deviceAddr = DeviceManager.deviceAddrOf( addresses );

        NFS4IoDevice newDevice = new NFS4IoDevice(mdsID , deviceAddr);
        NFSv41DeviceManagerFactory.getDeviceManager().addIoDevice(newDevice, layoutiomode4.LAYOUTIOMODE4_ANY);

        List<NFS4IoDevice> deviceList = NFSv41DeviceManagerFactory.getDeviceManager().getIoDeviceList();

        int deviceListSize = deviceList.size();
        if (deviceListSize > _args.opgetdevicelist.gdla_maxdevices.value.value) {
            deviceListSize = _args.opgetdevicelist.gdla_maxdevices.value.value;
        }

        /*
         * FIXME: protect against empty list
         */
        res.gdlr_resok4.gdlr_deviceid_list = new deviceid4[deviceListSize];
        res.gdlr_resok4.gdlr_deviceid_list[0] = new deviceid4( mdsID );

        for (int i = 0; i < deviceListSize; i++) {
            NFS4IoDevice device = deviceList.get(i);
            res.gdlr_resok4.gdlr_deviceid_list[i] = new deviceid4(device.getDeviceId());
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

    return new NFSv4OperationResult(_result, res.gdlr_status);

    }


    private static byte[] id2deviceid(int id) {


        byte[] puffer = Integer.toString(id).getBytes();
        byte[] devData = new byte[nfs4_prot.NFS4_DEVICEID4_SIZE];

        System.arraycopy(puffer, 0, devData, 0, puffer.length);

        return devData;

    }

}
