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

public class EXCHANGE_ID4resok implements XdrAble {
    public clientid4 eir_clientid;
    public sequenceid4 eir_sequenceid;
    public uint32_t eir_flags;
    public state_protect4_r eir_state_protect;
    public server_owner4 eir_server_owner;
    public byte [] eir_server_scope;
    public nfs_impl_id4 [] eir_server_impl_id;

    public EXCHANGE_ID4resok() {
    }

    public EXCHANGE_ID4resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        eir_clientid.xdrEncode(xdr);
        eir_sequenceid.xdrEncode(xdr);
        eir_flags.xdrEncode(xdr);
        eir_state_protect.xdrEncode(xdr);
        eir_server_owner.xdrEncode(xdr);
        xdr.xdrEncodeDynamicOpaque(eir_server_scope);
        { int $size = eir_server_impl_id.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { eir_server_impl_id[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        eir_clientid = new clientid4(xdr);
        eir_sequenceid = new sequenceid4(xdr);
        eir_flags = new uint32_t(xdr);
        eir_state_protect = new state_protect4_r(xdr);
        eir_server_owner = new server_owner4(xdr);
        eir_server_scope = xdr.xdrDecodeDynamicOpaque();
        { int $size = xdr.xdrDecodeInt(); eir_server_impl_id = new nfs_impl_id4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { eir_server_impl_id[$idx] = new nfs_impl_id4(xdr); } }
    }

}
// End of EXCHANGE_ID4resok.java
