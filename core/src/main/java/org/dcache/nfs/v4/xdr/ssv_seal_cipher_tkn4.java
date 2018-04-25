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

public class ssv_seal_cipher_tkn4 implements XdrAble {
    public uint32_t ssct_ssv_seq;
    public byte [] ssct_iv;
    public byte [] ssct_encr_data;
    public byte [] ssct_hmac;

    public ssv_seal_cipher_tkn4() {
    }

    public ssv_seal_cipher_tkn4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        ssct_ssv_seq.xdrEncode(xdr);
        xdr.xdrEncodeDynamicOpaque(ssct_iv);
        xdr.xdrEncodeDynamicOpaque(ssct_encr_data);
        xdr.xdrEncodeDynamicOpaque(ssct_hmac);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        ssct_ssv_seq = new uint32_t(xdr);
        ssct_iv = xdr.xdrDecodeDynamicOpaque();
        ssct_encr_data = xdr.xdrDecodeDynamicOpaque();
        ssct_hmac = xdr.xdrDecodeDynamicOpaque();
    }

}
// End of ssv_seal_cipher_tkn4.java
