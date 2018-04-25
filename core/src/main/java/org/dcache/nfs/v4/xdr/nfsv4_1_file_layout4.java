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

public class nfsv4_1_file_layout4 implements XdrAble {
    public deviceid4 nfl_deviceid;
    public nfl_util4 nfl_util;
    public uint32_t nfl_first_stripe_index;
    public offset4 nfl_pattern_offset;
    public nfs_fh4 [] nfl_fh_list;

    public nfsv4_1_file_layout4() {
    }

    public nfsv4_1_file_layout4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        nfl_deviceid.xdrEncode(xdr);
        nfl_util.xdrEncode(xdr);
        nfl_first_stripe_index.xdrEncode(xdr);
        nfl_pattern_offset.xdrEncode(xdr);
        { int $size = nfl_fh_list.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { nfl_fh_list[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        nfl_deviceid = new deviceid4(xdr);
        nfl_util = new nfl_util4(xdr);
        nfl_first_stripe_index = new uint32_t(xdr);
        nfl_pattern_offset = new offset4(xdr);
        { int $size = xdr.xdrDecodeInt(); nfl_fh_list = new nfs_fh4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { nfl_fh_list[$idx] = new nfs_fh4(xdr); } }
    }

}
// End of nfsv4_1_file_layout4.java
