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
package org.dcache.chimera.nfs.v4.xdr;
import org.dcache.xdr.*;
import java.io.IOException;

public class acemask4 implements XdrAble {

    public uint32_t value;

    public acemask4() {
    }

    public acemask4(uint32_t value) {
        this.value = value;
    }

    public acemask4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        value.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = new uint32_t(xdr);
    }

    public static acemask4 allOf(acemask4... masks) {
        int mask = 0;
        for (acemask4 acemask : masks) {
            mask |= acemask.value.value;
        }
        return new acemask4(new uint32_t(mask));
    }

    public static acemask4 clear(acemask4 acemask, acemask4 clear) {
        int mask = (acemask.value.value | clear.value.value) & ~acemask.value.value;
        return new acemask4(new uint32_t(mask));
    }
}
// End of acemask4.java
