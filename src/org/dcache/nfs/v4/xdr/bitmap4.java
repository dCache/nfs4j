/*
 * Copyright (c) 2009 - 2013 Deutsches Elektronen-Synchroton,
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
import java.util.Iterator;
import java.util.NoSuchElementException;

public class bitmap4 implements XdrAble, Iterable<Integer> {

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
        int n = max/Integer.SIZE + 1;
        bitmap4 bitmap = new bitmap4(new uint32_t[n]);
        for(int i = 0; i < n; i++) {
            bitmap.value[i] = new uint32_t();
        }

        /*
         * and populate them
         */
        for (int v : values) {
            int bit = v % Integer.SIZE;
            bitmap.value[v / Integer.SIZE].value |= 1 << bit;
        }
        return bitmap;
    }

    /**
     * Set the specified bit in this bitmap. Auto-grow bitmap size if required.
     *
     * @param bit to set
     */
    public void set(int bit) {
        ensureCapacity(bit);
        value[bit / Integer.SIZE].value |= 1 << bit % Integer.SIZE;
    }

    /**
     * Test whatever the bit is set in this bitmap.
     *
     * @param bit to test
     * @return true if bit is set
     */
    public boolean isSet(int bit) {

        int bitmapIdx = bit / Integer.SIZE;
        if (value == null || bitmapIdx  + 1 > value.length) {
            return false;
        }
        return ((value[bitmapIdx].value >> bit % Integer.SIZE) & 1) > 0;
    }

    private void ensureCapacity(int bit) {

        int expected = bit / Integer.SIZE + 1;

        if (value == null || value.length < expected) {
            uint32_t[] newBitmap = new uint32_t[expected];
            int initIndex = value == null ? 0 : value.length;
            if (value != null) {
                System.arraycopy(value, 0, newBitmap, 0, value.length);
            }
            for (int i = initIndex; i < newBitmap.length; i++) {
                newBitmap[i] = new uint32_t(0);
            }
            value = newBitmap;
        }
    }

    /**
     * Create an {@link Iterator} over all set bits.
     */
    @Override
    public Iterator<Integer> iterator() {

        return new Iterator<Integer>() {
            int currentIndex = -1;

            @Override
            public boolean hasNext() {
                if (value == null || currentIndex > value.length * Integer.SIZE) {
                    return false;
                }

                currentIndex++;
                for (; currentIndex < value.length * Integer.SIZE; currentIndex++) {
                    int bitmapIdx = currentIndex / Integer.SIZE;
                    int bit = (value[bitmapIdx].value >> currentIndex % Integer.SIZE) & 1;
                    if (bit != 0) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Integer next() {
                checkPosition();
                return currentIndex;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void checkPosition() {
                if (value == null || currentIndex < 0 || currentIndex > value.length * Integer.SIZE) {
                    throw new NoSuchElementException();
                }
            }
        };
    }
}
// End of bitmap4.java
