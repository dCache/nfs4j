/*
 * Copyright (c) 2009 - 2025 Deutsches Elektronen-Synchroton,
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
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class nfs_fh4 implements XdrAble {

    public byte [] value;

    public nfs_fh4() {
    }

    public nfs_fh4(byte [] value) {
        this.value = value;
    }

    public nfs_fh4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeDynamicOpaque(value);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeDynamicOpaque();
    }

    @Override
    public String toString() {
        return BaseEncoding.base16().lowerCase().encode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        nfs_fh4 nfsFh4 = (nfs_fh4) o;
        return Arrays.equals(value, nfsFh4.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
// End of nfs_fh4.java
