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
import org.dcache.xdr.*;
import java.io.IOException;

public class bitmap4 implements XdrAble {

    public uint32_t [] value;

    public bitmap4() {
    }

    public bitmap4(uint32_t [] value) {
        this.value = value;
    }

    public bitmap4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = value.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { value[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = xdr.xdrDecodeInt(); value = new uint32_t[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { value[$idx] = new uint32_t(xdr); } }
    }

    /**
     * Create a {@link bitmap4} for given values.
     *
     * @param values
     * @throws IllegalArgumentException if no values are provided
     * @return bitmap
     */
    public static bitmap4 of(int ... values) {
        if(values.length == 0) {
            throw new IllegalArgumentException("No values provided");
        }

        /*
         * find max value to calculate bitmap size
         */
        int max = values[0];
        for(int v: values) {
            if( v > max) max = v;
        }

        /*
         * ceate a bitmap to hold values
         */
        int n = max/32 +1;
        bitmap4 bitmap = new bitmap4(new uint32_t[n]);
        for(int i = 0; i < n; i++) {
            bitmap.value[i] = new uint32_t();
        }

        /*
         * and populate them
         */
        for (int v : values) {
            int bit = v - (32 * (v / 32));
            bitmap.value[v / 32].value |= 1 << bit;
        }
        return bitmap;
    }
}
// End of bitmap4.java
