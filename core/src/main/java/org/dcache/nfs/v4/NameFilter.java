/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4;

import com.google.common.base.Utf8;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadNameException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.NameTooLongException;

class NameFilter {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    /* utility calls */
    private NameFilter(){}

    /**
     * Validate name and convert into UTB8 {@link String}. Same as {@link NameFilter#convertPath(byte[])},
     * except, characters '.', '..', and '/' are not will throw BadNameException.
     * The valid name length must not exceed {@link NFSv4Defaults#NFS4_MAXFILENAME}.
     *
     * @param bytes to convert
     * @return string
     * @throws ChimeraNFSException if provided {@code bytes} are not a UTF8 encoded.
     */
    public static String convertName(byte[] bytes) throws ChimeraNFSException {

        String name = convertPath(bytes);

        if (name.length() > NFSv4Defaults.NFS4_MAXFILENAME) {
            throw new NameTooLongException("name too long");
        }

        if (name.equals(".") || name.equals("..")) {
            throw new BadNameException("bad name '.' or '..'");
        }

        if (name.indexOf('/') != -1) {
            throw new BadNameException("name with slash '/'");
        }

        return name;
    }

    /**
     * Validate path and convert into UTB8 {@link String}.
     *
     * @param bytes to convert
     * @return string
     * @throws ChimeraNFSException if provided {@code bytes} are not a UTF8
     * encoded.
     */
    public static String convertPath(byte[] bytes) throws ChimeraNFSException {

        if (!Utf8.isWellFormed(bytes)) {
            throw new InvalException("invalid utf8 name");
        }

        String name = new String(bytes, UTF8);

        if (name.length() == 0) {
            throw new InvalException("bad path name");
        }

        if (name.indexOf('\0') != -1) {
            throw new BadNameException("name with null");
        }

        return name;
    }
}
