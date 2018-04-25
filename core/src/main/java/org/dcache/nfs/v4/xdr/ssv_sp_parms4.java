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

public class ssv_sp_parms4 implements XdrAble {
    public state_protect_ops4 ssp_ops;
    public sec_oid4 [] ssp_hash_algs;
    public sec_oid4 [] ssp_encr_algs;
    public uint32_t ssp_window;
    public uint32_t ssp_num_gss_handles;

    public ssv_sp_parms4() {
    }

    public ssv_sp_parms4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        ssp_ops.xdrEncode(xdr);
        { int $size = ssp_hash_algs.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { ssp_hash_algs[$idx].xdrEncode(xdr); } }
        { int $size = ssp_encr_algs.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { ssp_encr_algs[$idx].xdrEncode(xdr); } }
        ssp_window.xdrEncode(xdr);
        ssp_num_gss_handles.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        ssp_ops = new state_protect_ops4(xdr);
        { int $size = xdr.xdrDecodeInt(); ssp_hash_algs = new sec_oid4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { ssp_hash_algs[$idx] = new sec_oid4(xdr); } }
        { int $size = xdr.xdrDecodeInt(); ssp_encr_algs = new sec_oid4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { ssp_encr_algs[$idx] = new sec_oid4(xdr); } }
        ssp_window = new uint32_t(xdr);
        ssp_num_gss_handles = new uint32_t(xdr);
    }

}
// End of ssv_sp_parms4.java
