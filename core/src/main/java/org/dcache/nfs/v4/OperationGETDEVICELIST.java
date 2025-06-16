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

import java.io.IOException;
import java.util.List;

import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.TooSmallException;
import org.dcache.nfs.v4.xdr.GETDEVICELIST4res;
import org.dcache.nfs.v4.xdr.GETDEVICELIST4resok;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_cookie4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationGETDEVICELIST extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationGETDEVICELIST.class);

    public OperationGETDEVICELIST(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_GETDEVICELIST);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final GETDEVICELIST4res res = result.opgetdevicelist;
        final NFSv41DeviceManager pnfsDeviceManager = context
                .getDeviceManager()
                .orElseThrow(() -> new NotSuppException("pNFS device manager not configured"));

        /*
         * GETDEVICELIST This operation returns an array of items (devlist_item4) that establish the association between
         * the short deviceid4 and the addressing information for that device, for a particular layout type.
         */

        if (_args.opgetdevicelist.gdla_maxdevices.value < 0) {
            throw new InvalException("negative maxcount");
        }

        if (_args.opgetdevicelist.gdla_maxdevices.value < 1) {
            throw new TooSmallException("device list too small");
        }

        res.gdlr_resok4 = new GETDEVICELIST4resok();

        res.gdlr_resok4.gdlr_cookie = new nfs_cookie4(1);
        res.gdlr_resok4.gdlr_cookieverf = new verifier4();
        res.gdlr_resok4.gdlr_cookieverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        List<deviceid4> deviceIDs = pnfsDeviceManager.getDeviceList(context, _args.opgetdevicelist);

        int deviceListSize = Math.min(deviceIDs.size(), _args.opgetdevicelist.gdla_maxdevices.value);

        res.gdlr_resok4.gdlr_deviceid_list = new deviceid4[deviceListSize];

        for (int i = 0; i < deviceListSize; i++) {
            deviceid4 deviceId = deviceIDs.get(i);
            res.gdlr_resok4.gdlr_deviceid_list[i] = deviceId;
        }

        _log.debug("GETDEVICELIST4: new list of #{}, maxcount {}",
                res.gdlr_resok4.gdlr_deviceid_list.length,
                _args.opgetdevicelist.gdla_maxdevices.value);

        /*
         * we reply only one dummy entry. The rest is dynamic
         */
        res.gdlr_resok4.gdlr_eof = true;
        res.gdlr_status = nfsstat.NFS_OK;
    }
}
