/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.client;

import org.dcache.nfs.v4.xdr.GETDEVICEINFO4args;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;

public class GetdeviceinfoStub {

    public static nfs_argop4 generateRequest(deviceid4 devId) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICEINFO;
        op.opgetdeviceinfo = new GETDEVICEINFO4args();

        op.opgetdeviceinfo.gdia_device_id = devId;
        op.opgetdeviceinfo.gdia_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        op.opgetdeviceinfo.gdia_maxcount = new count4(0xffffff);
        op.opgetdeviceinfo.gdia_notify_types = new bitmap4( new int[] {0});

        return op;

    }
}
