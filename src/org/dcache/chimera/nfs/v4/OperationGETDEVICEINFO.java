package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.layouttype4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.GETDEVICEINFO4res;
import org.dcache.chimera.nfs.v4.xdr.GETDEVICEINFO4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;

import org.apache.log4j.Logger;

public class OperationGETDEVICEINFO extends AbstractNFSv4Operation {

    private static final Logger _log = Logger
            .getLogger(OperationGETDEVICEINFO.class.getName());

    OperationGETDEVICEINFO(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_GETDEVICEINFO);
    }

    @Override
    public boolean process(CompoundContext context) {

        /*
         * GETDEVICEINFO.
         * returns the mapping of device ID to storage device address.
         */
        GETDEVICEINFO4res res = new GETDEVICEINFO4res();

        DeviceID deviceId = DeviceID.valueOf(_args.opgetdeviceinfo.gdia_device_id);

        if (_log.isDebugEnabled()) {
            _log.debug("             Info for #" + deviceId);
            _log.debug("             type for #"
                    + _args.opgetdeviceinfo.gdia_layout_type);
        }
        try {

            res.gdir_resok4 = new GETDEVICEINFO4resok();

            NFS4IoDevice device = NFSv41DeviceManagerFactory.getDeviceManager()
                    .getIoDevice(deviceId);

            if (device == null) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "invalid device id");
            }

            res.gdir_resok4.gdir_device_addr = device.getDeviceAddr();
            res.gdir_resok4.gdir_device_addr.da_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
            res.gdir_resok4.gdir_notification = new bitmap4();
            res.gdir_resok4.gdir_notification.value = new uint32_t[1];
            res.gdir_resok4.gdir_notification.value[0] = new uint32_t(0);

            res.gdir_status = nfsstat4.NFS4_OK;

        } catch (ChimeraNFSException he) {
            if (_log.isDebugEnabled()) {
                _log.debug("GETDEVICEINFO: " + he.getMessage());
            }
            res.gdir_status = he.getStatus();
        } catch (Exception e) {
            _log.error("GETDEVICEINFO4: ", e);
            res.gdir_status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

        _result.opgetdeviceinfo = res;

        context.processedOperations().add(_result);
        return res.gdir_status == nfsstat4.NFS4_OK;

    }

}
