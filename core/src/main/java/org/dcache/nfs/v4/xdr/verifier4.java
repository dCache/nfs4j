/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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

import com.google.common.io.BaseEncoding;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.util.Bytes;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;
import java.util.Arrays;

public class verifier4 implements XdrAble {

    public byte [] value;

    public verifier4() {
    }

    public verifier4(byte [] value) {
        this.value = value;
    }

    public verifier4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeOpaque(value, nfs4_prot.NFS4_VERIFIER_SIZE);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeOpaque(nfs4_prot.NFS4_VERIFIER_SIZE);
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {

        if( o == this) return true;
        if(!(o instanceof verifier4)) return false;

        verifier4 other = (verifier4)o;

        return Arrays.equals(other.value, this.value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(BaseEncoding.base16().lowerCase().encode(value)).append(']');
        return sb.toString();
    }

    public static verifier4 valueOf(long value) {
        byte[] bytes = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];
        Bytes.putLong(bytes, 0, value);
        return new verifier4(bytes);
    }
}
// End of verifier4.java
