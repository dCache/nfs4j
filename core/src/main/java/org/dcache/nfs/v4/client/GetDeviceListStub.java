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
package org.dcache.nfs.v4.client;

import java.io.IOException;

import org.dcache.nfs.v4.xdr.GETDEVICELIST4args;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_cookie4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout_ds_addr4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrBuffer;
import org.dcache.xdr.XdrDecodingStream;

public class GetDeviceListStub {

    public static nfs_argop4 normal() {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICELIST;
        op.opgetdevicelist = new GETDEVICELIST4args();

        op.opgetdevicelist.gdla_cookie = new nfs_cookie4(0);

        op.opgetdevicelist.gdla_cookieverf = new verifier4();
        op.opgetdevicelist.gdla_cookieverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        op.opgetdevicelist.gdla_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();

        op.opgetdevicelist.gdla_maxdevices = new count4(256);

        return op;
    }

    public static nfsv4_1_file_layout_ds_addr4 decodeFileDevice(byte[] data)
            throws OncRpcException, IOException {
        XdrDecodingStream xdr = new XdrBuffer(data);

        nfsv4_1_file_layout_ds_addr4 device = new nfsv4_1_file_layout_ds_addr4();

        xdr.beginDecoding();
        device.xdrDecode(xdr);
        xdr.endDecoding();

        return device;
    }

}
