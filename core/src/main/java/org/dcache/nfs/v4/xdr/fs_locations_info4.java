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

public class fs_locations_info4 implements XdrAble {
    public uint32_t fli_flags;
    public int32_t fli_valid_for;
    public pathname4 fli_fs_root;
    public fs_locations_item4 [] fli_items;

    public fs_locations_info4() {
    }

    public fs_locations_info4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        fli_flags.xdrEncode(xdr);
        fli_valid_for.xdrEncode(xdr);
        fli_fs_root.xdrEncode(xdr);
        { int $size = fli_items.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { fli_items[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        fli_flags = new uint32_t(xdr);
        fli_valid_for = new int32_t(xdr);
        fli_fs_root = new pathname4(xdr);
        { int $size = xdr.xdrDecodeInt(); fli_items = new fs_locations_item4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { fli_items[$idx] = new fs_locations_item4(xdr); } }
    }

}
// End of fs_locations_info4.java
