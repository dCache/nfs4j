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
import java.util.Objects;

/**
 * Describes something that can be used as a key for {@link java.util.HashMap} and that can be converted to a
 * {@code byte[]} and a Base64 string representation.
 * <p>
 * Note that {@link Opaque}s that are <em>stored</em> in {@link java.util.HashMap} need to be immutable. Call
 * {@link #toImmutableOpaque()} when necessary (e.g., when using {@link java.util.HashMap#put(Object, Object)},
 * {@link java.util.HashMap#computeIfAbsent(Object, java.util.function.Function)}, etc.
 */
public interface Opaque {
    /**
     * Returns an immutable {@link Opaque} instance based on a copy of the given bytes.
     * 
     * @param bytes The bytes.
     * @return The {@link Opaque} instance.
     */
    static Opaque forBytes(byte[] bytes) {
        return new OpaqueImpl(bytes.clone());
    }

    /**
     * Returns an immutable {@link Opaque} instance based on a copy of the {@code length} bytes from the given
     * {@link ByteBuffer}.
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
     * Returns a <em>mutable</em> {@link Opaque} instance backed on the byte contents of the given {@link ByteBuffer},
     * for the given number of bytes starting from the given absolute index.
     * <p>
     * Note that the returned {@link Opaque} is typically not suitable for <em>storing</em> in a
     * {@link java.util.HashMap}, but merely for lookups. Call {@link #toImmutableOpaque()} when necessary.
     * 
     * @param buf The buffer backing the {@link Opaque}.
     * @param index The absolute index to start from.
     * @param length The number of bytes.
     * @return The {@link Opaque} instance.
     * @see #toImmutableOpaque()
     */
    static Opaque forMutableByteBuffer(ByteBuffer buf, int index, int length) {
        return new OpaqueBufferImpl(buf, index, length);
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
     * Returns an immutable {@link Opaque}, which may be the instance itself if it is already immutable.
     * 
     * @return An immutable opaque.
     */
    Opaque toImmutableOpaque();

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
            } else if (o instanceof OpaqueBufferImpl) {
                OpaqueBufferImpl other = (OpaqueBufferImpl) o;
                if (other.numBytes() != _opaque.length) {
                    return false;
                }
                ByteBuffer otherBuf = other.buf;
                int otherIndex = other.index;
                for (int i = 0, n = _opaque.length, oi = otherIndex; i < n; i++, oi++) {
                    if (_opaque[i] != otherBuf.get(oi)) {
                        return false;
                    }
                }
                return true;
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

        @Override
        public Opaque toImmutableOpaque() {
            return this;
        }
    }

    final class OpaqueBufferImpl implements Opaque {
        private final ByteBuffer buf;
        private final int index;
        private final int length;

        private OpaqueBufferImpl(ByteBuffer buf, int index, int length) {
            this.buf = Objects.requireNonNull(buf);
            this.index = index;
            this.length = length;
        }

        @Override
        public byte[] toBytes() {
            byte[] bytes = new byte[length];
            buf.get(index, bytes);
            return bytes;
        }

        @Override
        public int numBytes() {
            return length;
        }

        @Override
        public String toBase64() {
            return Base64.getEncoder().withoutPadding().encodeToString(toBytes());
        }

        @Override
        public Opaque toImmutableOpaque() {
            return Opaque.forBytes(toBytes());
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (int i = index, n = index + length; i < n; i++) {
                byte element = buf.get(i);
                result = 31 * result + element;
            }

            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Opaque)) {
                return false;
            }
            if (length != ((Opaque) o).numBytes()) {
                return false;
            }

            if (o instanceof OpaqueImpl) {
                byte[] otherBytes = ((OpaqueImpl) o)._opaque;
                for (int i = index, n = index + length, oi = 0; i < n; i++, oi++) {
                    if (buf.get(i) != otherBytes[oi]) {
                        return false;
                    }
                }
                return true;
            } else if (o instanceof OpaqueBufferImpl) {
                OpaqueBufferImpl other = (OpaqueBufferImpl) o;
                ByteBuffer otherBuf = other.buf;
                int otherIndex = other.index;
                for (int i = index, n = index + length, oi = otherIndex; i < n; i++, oi++) {
                    if (buf.get(i) != otherBuf.get(oi)) {
                        return false;
                    }
                }
                return true;
            } else {
                return toImmutableOpaque().equals(o);
            }
        }

        @Override
        public String toString() {
            return super.toString() + "[" + toBase64() + "]";
        }
    }
}
