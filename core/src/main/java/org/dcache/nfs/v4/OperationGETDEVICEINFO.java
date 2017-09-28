/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.nfs.v4;

import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.GETDEVICEINFO4res;
import org.dcache.nfs.v4.xdr.GETDEVICEINFO4resok;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.device_addr4;
import java.io.IOException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.v4.xdr.layouttype4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcache.nfs.v4.xdr.notify_deviceid_type4.*;

public class OperationGETDEVICEINFO extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationGETDEVICEINFO.class);

    public OperationGETDEVICEINFO(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_GETDEVICEINFO);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        /*
         * GETDEVICEINFO. returns the mapping of device ID to storage device
         * address.
         */
        final GETDEVICEINFO4res res = result.opgetdeviceinfo;

        deviceid4 deviceId = _args.opgetdeviceinfo.gdia_device_id;

        _log.debug("Get device info for device [{}], type {} ", deviceId, _args.opgetdeviceinfo.gdia_layout_type);

        res.gdir_resok4 = new GETDEVICEINFO4resok();

        layouttype4 layoutType = layouttype4.valueOf(_args.opgetdeviceinfo.gdia_layout_type);
        device_addr4 deviceInfo = context.getDeviceManager().getDeviceInfo(context, deviceId, layoutType);

        if (deviceInfo == null) {
            throw new NoEntException("invalid deviceInfo id [" + deviceId +"]");
        }

        res.gdir_resok4.gdir_device_addr = deviceInfo;
        res.gdir_resok4.gdir_notification = new bitmap4();
        /*
         * provide faked notification only if client expects them
         */
        if (_args.opgetdeviceinfo.gdia_notify_types.isSet(NOTIFY_DEVICEID4_CHANGE)) {
            res.gdir_resok4.gdir_notification.set(NOTIFY_DEVICEID4_CHANGE);
        }
        if (_args.opgetdeviceinfo.gdia_notify_types.isSet(NOTIFY_DEVICEID4_DELETE)) {
            res.gdir_resok4.gdir_notification.set(NOTIFY_DEVICEID4_DELETE);
        }

        res.gdir_status = nfsstat.NFS_OK;
    }
}
