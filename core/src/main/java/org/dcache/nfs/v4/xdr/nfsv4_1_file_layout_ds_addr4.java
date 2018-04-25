/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.xdr;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;

public class nfsv4_1_file_layout_ds_addr4 implements XdrAble {
    public uint32_t [] nflda_stripe_indices;
    public multipath_list4 [] nflda_multipath_ds_list;

    public nfsv4_1_file_layout_ds_addr4() {
    }

    public nfsv4_1_file_layout_ds_addr4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = nflda_stripe_indices.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { nflda_stripe_indices[$idx].xdrEncode(xdr); } }
        { int $size = nflda_multipath_ds_list.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { nflda_multipath_ds_list[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = xdr.xdrDecodeInt(); nflda_stripe_indices = new uint32_t[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { nflda_stripe_indices[$idx] = new uint32_t(xdr); } }
        { int $size = xdr.xdrDecodeInt(); nflda_multipath_ds_list = new multipath_list4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { nflda_multipath_ds_list[$idx] = new multipath_list4(xdr); } }
    }

}
// End of nfsv4_1_file_layout_ds_addr4.java
