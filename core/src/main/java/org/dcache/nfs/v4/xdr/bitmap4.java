/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
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
import java.util.Iterator;
import java.util.NoSuchElementException;

public class bitmap4 implements XdrAble, Iterable<Integer> {

    public int[] value;

    public bitmap4() {
        this(new int[] {0, 0});
    }

    public bitmap4(int [] value) {
        this.value = value;
    }

    public bitmap4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = value.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { xdr.xdrEncodeInt(value[$idx]); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = xdr.xdrDecodeInt(); value = new int[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { value[$idx] = xdr.xdrDecodeInt(); } }
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
        bitmap4 bitmap = new bitmap4(new int[n]);

        /*
         * and populate them
         */
        for (int v : values) {
            int bit = v % Integer.SIZE;
            bitmap.value[v / Integer.SIZE] |= 1 << bit;
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
        value[bit / Integer.SIZE] |= 1 << bit % Integer.SIZE;
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
        return ((value[bitmapIdx] >> bit % Integer.SIZE) & 1) > 0;
    }

    private void ensureCapacity(int bit) {

        int expected = bit / Integer.SIZE + 1;

        if (value == null || value.length < expected) {
            int[] newBitmap = new int[expected];
            if (value != null) {
                System.arraycopy(value, 0, newBitmap, 0, value.length);
            }
            value = newBitmap;
        }
    }

    /**
     * Returns <tt>true</tt> if, and only if, none of the bits are set.
     * @return <tt>true</tt> if none of the bits are set, otherwise
     * <tt>false</tt>
     */
    public boolean isEmpty() {

        if (value == null) {
            return true;
        }

        for (int mask : value) {
            if (mask != 0) {
                return false;
            }
        }
        return true;
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
                    int bit = (value[bitmapIdx] >> currentIndex % Integer.SIZE) & 1;
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

    @Override
    public int hashCode() {
        /*
         * adopted version of Arrays#hashCode: all trailing zero elements must be ignored
         */

        int hash = 1;

        if (value != null) {
            int n;
            // point n to last non zero element
            for(n = value.length; n > 0 && value[n-1] == 0; n--)
                ;

            for (int i = 0; i < n; i++) {
                hash = 31 * hash + value[i];
            }
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        /**
         * empty bitmaps are equal independent from number of elements in the array
         */
        final bitmap4 other = (bitmap4) obj;
        int len = 0;
        int olen = 0;

        if (value != null) {
            len = value.length;
        }

        if(other.value != null) {
            olen = other.value.length;
        }

        int i;
        int match = Math.min(len, olen);
        for(i = 0; i < match; i++) {
            if ( value[i] != other.value[i] ) {
                return false;
            }
        }

        // now only all zero bits allowed. I point to last common element
        for (int k = i; k < len; k++) {
            if (value[k] != 0) {
                return false;
            }
        }

        for (int k = i; k < olen; k++) {
            if (other.value[k] != 0) {
                return false;
            }
        }

        return true;
    }
}
// End of bitmap4.java
