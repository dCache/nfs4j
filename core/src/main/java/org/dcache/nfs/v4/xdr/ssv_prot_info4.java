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

public class ssv_prot_info4 implements XdrAble {
    public state_protect_ops4 spi_ops;
    public uint32_t spi_hash_alg;
    public uint32_t spi_encr_alg;
    public uint32_t spi_ssv_len;
    public uint32_t spi_window;
    public gsshandle4_t [] spi_handles;

    public ssv_prot_info4() {
    }

    public ssv_prot_info4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        spi_ops.xdrEncode(xdr);
        spi_hash_alg.xdrEncode(xdr);
        spi_encr_alg.xdrEncode(xdr);
        spi_ssv_len.xdrEncode(xdr);
        spi_window.xdrEncode(xdr);
        { int $size = spi_handles.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { spi_handles[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        spi_ops = new state_protect_ops4(xdr);
        spi_hash_alg = new uint32_t(xdr);
        spi_encr_alg = new uint32_t(xdr);
        spi_ssv_len = new uint32_t(xdr);
        spi_window = new uint32_t(xdr);
        { int $size = xdr.xdrDecodeInt(); spi_handles = new gsshandle4_t[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { spi_handles[$idx] = new gsshandle4_t(xdr); } }
    }

}
// End of ssv_prot_info4.java
