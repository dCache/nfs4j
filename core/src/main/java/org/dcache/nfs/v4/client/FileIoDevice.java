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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.dcache.nfs.v4.xdr.netaddr4;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout_ds_addr4;
import org.dcache.oncrpc4j.rpc.net.InetSocketAddresses;

public class FileIoDevice {

    private final InetSocketAddress[] _ds_list;
    private final int[] _stripe_indices;

    public FileIoDevice(nfsv4_1_file_layout_ds_addr4 addr)
            throws UnknownHostException {

        _stripe_indices = new int[addr.nflda_stripe_indices.length];
        for (int i = 0; i < _stripe_indices.length; i++) {
            _stripe_indices[i] = addr.nflda_stripe_indices[i].value;
        }
        _ds_list = new InetSocketAddress[addr.nflda_multipath_ds_list.length];
        for (int i = 0; i < _ds_list.length; i++) {
            for(netaddr4 na : addr.nflda_multipath_ds_list[i].value) {
                if (na.na_r_netid.equals("tcp") || na.na_r_netid.equals("tcp6") ) {
                    _ds_list[i] = InetSocketAddresses.forUaddrString(na.na_r_addr);
                    break;
                }
            }
        }
    }

    /*
     * as defined in rfc5661 13.4
     */
    public InetSocketAddress of(long pattern_offset, int unit_size, long offset, int len, int first_stripe_index) {
        long relative_offset = offset - pattern_offset;
        int suIndex = (int) Math.floor(relative_offset / unit_size);

        int index = (suIndex + first_stripe_index) % _stripe_indices.length;
        return _ds_list[_stripe_indices[index]];
    }

    @Override
    public String toString() {
        return Arrays.toString(_ds_list);
    }
}
