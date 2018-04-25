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

public class EXCHANGE_ID4args implements XdrAble {
    public client_owner4 eia_clientowner;
    public uint32_t eia_flags;
    public state_protect4_a eia_state_protect;
    public nfs_impl_id4 [] eia_client_impl_id;

    public EXCHANGE_ID4args() {
    }

    public EXCHANGE_ID4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        eia_clientowner.xdrEncode(xdr);
        eia_flags.xdrEncode(xdr);
        eia_state_protect.xdrEncode(xdr);
        { int $size = eia_client_impl_id.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { eia_client_impl_id[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        eia_clientowner = new client_owner4(xdr);
        eia_flags = new uint32_t(xdr);
        eia_state_protect = new state_protect4_a(xdr);
        { int $size = xdr.xdrDecodeInt(); eia_client_impl_id = new nfs_impl_id4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { eia_client_impl_id[$idx] = new nfs_impl_id4(xdr); } }
    }

}
// End of EXCHANGE_ID4args.java
