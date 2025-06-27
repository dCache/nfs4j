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

import java.util.Arrays;
import java.util.Base64;

/**
 * Describes something that can be used as a key in {@link java.util.Map} and that can be converted to a {@code byte[]}
 * and a Base64 string representation.
 */
public interface Opaque {
    public static Opaque forBytes(byte[] bytes) {
        return new OpaqueImpl(bytes.clone());
    }

    byte[] toBytes();

    String toBase64();

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);

    final class OpaqueImpl implements Opaque {
        private final byte[] _opaque;
        private String base64 = null;

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
        public int hashCode() {
            return Arrays.hashCode(_opaque);
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
    }
}
