/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v3.xdr;
import org.dcache.xdr.*;
import org.dcache.utils.Bytes;
import java.io.IOException;
import java.util.Arrays;

public class cookieverf3 implements XdrAble {

    public byte [] value;

    public cookieverf3() {
    }

    public cookieverf3(byte [] value) {
        this.value = value;
    }

    public cookieverf3(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeOpaque(value, nfs3_prot.NFS3_COOKIEVERFSIZE);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeOpaque(nfs3_prot.NFS3_COOKIEVERFSIZE);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {

        if( obj == this ) return true;

        if( !(obj instanceof cookieverf3) ) return false;

        return Arrays.equals(this.value, ((cookieverf3)obj).value );
    }

    public static cookieverf3 valueOf(long value) {
        byte[] bytes = new byte[nfs3_prot.NFS3_COOKIEVERFSIZE];
        Bytes.putLong(bytes, 0, value);
        return new cookieverf3(bytes);
    }
}
// End of cookieverf3.java
