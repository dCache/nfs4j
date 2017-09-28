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
import java.net.InetSocketAddress;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.xdr.device_addr4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.layout_content4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.multipath_list4;
import org.dcache.nfs.v4.xdr.netaddr4;
import org.dcache.nfs.v4.xdr.nfl_util4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout_ds_addr4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrBuffer;
import org.glassfish.grizzly.Buffer;

import static com.google.common.base.Preconditions.checkArgument;
/**
 * layout driver for NFSv4.1 file layout type as defined in
 * <a href="https://www.ietf.org/rfc/rfc5661.txt">rfc5661</a>
 */
public class NfsV41FileLayoutDriver implements LayoutDriver {

    @Override
    public layouttype4 getLayoutType() {
        return layouttype4.LAYOUT4_NFSV4_1_FILES;
    }

    @Override
    public device_addr4 getDeviceAddress(InetSocketAddress... deviceAddress) {

        nfsv4_1_file_layout_ds_addr4 file_type = new nfsv4_1_file_layout_ds_addr4();

        file_type.nflda_multipath_ds_list = new multipath_list4[1];
        file_type.nflda_multipath_ds_list[0] = new multipath_list4();
        file_type.nflda_multipath_ds_list[0].value = new netaddr4[deviceAddress.length];

        for (int i = 0; i < deviceAddress.length; i++) {
            file_type.nflda_multipath_ds_list[0].value[i] = new netaddr4(deviceAddress[i]);
        }

        file_type.nflda_stripe_indices = new uint32_t[1];
        file_type.nflda_stripe_indices[0] = new uint32_t(0);

        XdrBuffer xdr = new XdrBuffer(128);
        try {
            xdr.beginEncoding();
            file_type.xdrEncode(xdr);
            xdr.endEncoding();
        } catch (OncRpcException e) {
            /* forced by interface, should never happen. */
            throw new RuntimeException("Unexpected OncRpcException:", e);
        } catch (IOException e) {
            /* forced by interface, should never happen. */
            throw new RuntimeException("Unexpected IOException:", e);
        }

        Buffer body = xdr.asBuffer();
        byte[] retBytes = new byte[body.remaining()];
        body.get(retBytes);

        device_addr4 addr = new device_addr4();
        addr.da_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        addr.da_addr_body = retBytes;

        return addr;
    }

    @Override
    public layout_content4 getLayoutContent(stateid4 stateid, int stripeSize, nfs_fh4 fh, deviceid4 ... deviceids) throws ServerFaultException {

        checkArgument(deviceids.length == 1, "Layout driver supports only one (1) device.");

        nfsv4_1_file_layout4 layout = new nfsv4_1_file_layout4();

        layout.nfl_deviceid = deviceids[0];

        /*
         * The number of elements in nfl_fh_list MUST be one of three values:
         *
         *  Zero.  This means that filehandles used for each data
         *  server are the same as the filehandle returned by the OPEN
         *  operation from the metadata server.
         *
         *  One.  This means that every data server uses the same
         *  filehandle: what is specified in nfl_fh_list[0].
         *
         *  The same number of elements in nflda_multipath_ds_list.
         *  Thus, in this case, when sending an I/O operation to any
         *  data server in nflda_multipath_ds_list[X], the filehandle
         *  in nfl_fh_list[X] MUST be used.
         */
        layout.nfl_fh_list = new nfs_fh4[1];
        layout.nfl_fh_list[0] = fh;

        layout.nfl_first_stripe_index = new uint32_t(0);
        layout.nfl_util = new nfl_util4(stripeSize & nfs4_prot.NFL4_UFLG_STRIPE_UNIT_SIZE_MASK);

        //where the striping pattern starts
        layout.nfl_pattern_offset = new offset4(0);

        XdrBuffer xdr = new XdrBuffer(512);
        xdr.beginEncoding();

        try {
            layout.xdrEncode(xdr);
        } catch (IOException e) {
            throw new ServerFaultException("failed to encode layout body");
        }
        xdr.endEncoding();

        Buffer xdrBody = xdr.asBuffer();
        byte[] body = new byte[xdrBody.remaining()];
        xdrBody.get(body);

        layout_content4 content = new layout_content4();
        content.loc_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        content.loc_body = body;

        return content;
    }

}
