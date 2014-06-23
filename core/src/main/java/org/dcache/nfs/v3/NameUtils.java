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
package org.dcache.nfs.v3;

import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.NameTooLongException;

/**
 * Utility class to manipulate filename object.
 */
public class NameUtils {

    private NameUtils() {}

    /**
     * Validate ${code filename} requirements.
     * @param filename
     * @throws AccessException if filename does not meet expected constrains
     * @throws NameTooLongException if filename is longer than negotiated with PATHCONF operation.
     */
    public static void checkFilename(String filename) throws AccessException, NameTooLongException {

        // FIXME: merge with NFSv4 defaults
        if (filename.length() > 256) {
            throw new NameTooLongException();
        }

        if (filename.length() == 0 || filename.indexOf('/') != -1 || filename.indexOf('\0') != -1 ) {
            throw new AccessException();
        }

    }
}
