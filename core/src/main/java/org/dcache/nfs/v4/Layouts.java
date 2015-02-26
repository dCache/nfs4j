/*
 * Copyright (c) 2015 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.nfl_util4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.uint32_t;

/**
 * A class which provides static methods acting on or generating layouts
 * @since 0.11
 */
public class Layouts {

    private Layouts() {}

    /**
     * Create a new {@code nfsv4_1_file_layout4} for a given {@code deviceid},
     * {@code sttripeSize} and {@code fileHandle}. The {@code fileHandle} will be
     * used by all data servers.
     * @param deviceid associated with this layout.
     * @param stripeSize number of bytes client must
     * @param fileHandle which must be used by client when talking to data servers.
     * @return new layout
     */
    public static nfsv4_1_file_layout4 newNfsFileLayout(deviceid4 deviceid, int stripeSize, nfs_fh4 fileHandle) {
        nfsv4_1_file_layout4 layout = new nfsv4_1_file_layout4();

        layout.nfl_deviceid = deviceid;

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
        layout.nfl_fh_list[0] = fileHandle;

        layout.nfl_first_stripe_index = new uint32_t(0);
        layout.nfl_util = new nfl_util4(stripeSize & nfs4_prot.NFL4_UFLG_STRIPE_UNIT_SIZE_MASK);

        //where the striping pattern starts
        layout.nfl_pattern_offset = new offset4(0);

        return layout;
    }
}
