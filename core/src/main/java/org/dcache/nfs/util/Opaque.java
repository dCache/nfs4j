/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

/**
 * Describes something that can be used as a key in {@link java.util.Map} and that can be converted to a {@code byte[]}
 * and a Base64 string representation.
 */
public interface Opaque {
    /**
     * Returns an {@link Opaque} instance based on a copy of the given bytes.
     * 
     * @param bytes The bytes.
     * @return The {@link Opaque} instance.
     */
    static Opaque forBytes(byte[] bytes) {
        return new OpaqueImpl(bytes.clone());
    }

    /**
     * Returns an {@link Opaque} instance based on a copy of the {@code length} bytes from the given {@link ByteBuffer}.
     * 
     * @param buf The buffer.
     * @param length The number of bytes.
     * @return The {@link Opaque} instance.
     */
    static Opaque forBytes(ByteBuffer buf, int length) {
        byte[] bytes = new byte[length];
        buf.get(bytes);

        return new OpaqueImpl(bytes);
    }

    /**
     * Default implementation for {@link #hashCode()}.
     * 
     * @param obj The instance object.
     * @return The hash code.
     * @see #hashCode()
     */
    static int defaultHashCode(Opaque obj) {
        return Arrays.hashCode(obj.toBytes());
    }

    /**
     * Default implementation for {@link #equals(Object)}.
     * 
     * @param obj The instance object.
     * @param other The other object.
     * @return {@code true} if equal.
     * @see #equals(Object)
     */
    static boolean defaultEquals(Opaque obj, Object other) {
        if (other == obj) {
            return true;
        }
        if (!(other instanceof Opaque)) {
            return false;
        }
        return Arrays.equals(obj.toBytes(), ((Opaque) other).toBytes());
    }

    /**
     * Returns a byte-representation of this opaque object.
     * 
     * @return A new array.
     */
    byte[] toBytes();

    /**
     * Returns the number of bytes in this opaque object;
     * 
     * @return The number of bytes;
     */
    int numBytes();

    /**
     * Returns a Base64 string representing this opaque object.
     * 
     * @return A Base64 string.
     */
    String toBase64();

    /**
     * Writes the bytes of this {@link Opaque} to the given {@link ByteBuffer}.
     * 
     * @param buf The target buffer.
     */
    default void putBytes(ByteBuffer buf) {
        buf.put(toBytes());
    }

    /**
     * Returns the hashCode based on the byte-representation of this instance.
     * <p>
     * This method must behave like {@link #defaultHashCode(Opaque)}, but may be optimized.
     * 
     * @return The hashCode.
     */
    @Override
    int hashCode();

    /**
     * Compares this object to another one.
     * <p>
     * This method must behave like {@link #defaultEquals(Opaque, Object)}, but may be optimized.
     * 
     * @return {@code true} if both objects are equal.
     */
    @Override
    boolean equals(Object o);

    final class OpaqueImpl implements Opaque {
        private final byte[] _opaque;
        private String base64 = null;
        private int hashCode;

        private OpaqueImpl(byte[] opaque) {
            _opaque = opaque;
        }

        @Override
        public byte[] toBytes() {
            return _opaque.clone();
        }

        @Override
        public String toBase64() {
            if (base64 == null) {
                base64 = Base64.getEncoder().withoutPadding().encodeToString(_opaque);
            }
            return base64;
        }

        @Override
        public void putBytes(ByteBuffer buf) {
            buf.put(_opaque);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Arrays.hashCode(_opaque);
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Opaque)) {
                return false;
            }

            if (o instanceof OpaqueImpl) {
                return Arrays.equals(_opaque, ((OpaqueImpl) o)._opaque);
            } else {
                return Arrays.equals(_opaque, ((Opaque) o).toBytes());
            }
        }

        /**
         * Returns a (potentially non-stable) debug string.
         * 
         * @see #toBase64()
         */
        @Override
        public String toString() {
            return super.toString() + "[" + toBase64() + "]";
        }

        @Override
        public int numBytes() {
            return _opaque.length;
        }
    }
}
