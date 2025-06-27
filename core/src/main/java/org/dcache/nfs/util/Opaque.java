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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;

import com.google.common.io.BaseEncoding;

/**
 * A helper class for opaque data manipulations. Enabled opaque date to be used as a key in {@link java.util.Collection}
 */
public class Opaque implements Serializable {

    private static final long serialVersionUID = 1532238396149112674L;

    private final byte[] _opaque;
    private String base64 = null;

    public static Opaque forBytes(byte[] bytes) {
        return new Opaque(bytes.clone());
    }

    private Opaque(byte[] opaque) {
        _opaque = opaque;
    }

    public byte[] asBytes() {
        return _opaque.clone();
    }

    public String getBase64() {
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

        return Arrays.equals(_opaque, ((Opaque) o)._opaque);
    }

    /**
     * Returns a (potentially non-stable) debug string.
     * 
     * @see #getBase64()
     */
    @Deprecated
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(BaseEncoding.base16().lowerCase().encode(_opaque)).append(']');
        return sb.toString();
    }
}
