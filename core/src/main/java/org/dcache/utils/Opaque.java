/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A helper class for opaque data manipulations.
 * Enabled opaque date to be used as a key in {@link java.util.Collection}
 */
public class Opaque implements Serializable {

    private static final long serialVersionUID = 1532238396149112674L;

    private final byte[] _opaque;

    public Opaque(byte[] opaque) {
        _opaque = opaque;
    }

    public byte[] getOpaque() {
        return _opaque;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(Bytes.toHexString(_opaque)).append(']');
        return sb.toString();
    }
}
