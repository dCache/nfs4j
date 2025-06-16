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
package org.dcache.nfs.v4;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.AbstractNFSv4Operation;
import org.dcache.nfs.v4.CompoundContext;
import org.dcache.nfs.v4.xdr.nfs_resop4;

import junit.framework.AssertionFailedError;

public class AssertNFS {

    public static void assertNFS(AbstractNFSv4Operation op,
            CompoundContext context, nfs_resop4 result, int expectedStatus) throws Exception {

        try {
            op.process(context, result);
            assertNFSStatus(expectedStatus, result.getStatus());
        } catch (ChimeraNFSException e) {
            assertNFSStatus(expectedStatus, e.getStatus());
        }
    }

    /**
     *
     * @param msg
     * @param expected
     * @param actual
     */
    public static void assertNFSStatus(String msg, int expected, int actual) {

        if (expected != actual) {
            String message = String.format("%s: expected:<%s> but was:<%s>",
                    msg, nfsstat.toString(expected), nfsstat.toString(actual));
            throw new AssertionFailedError(message);
        }

    }

    /**
     *
     * @param expected
     * @param actual
     */
    public static void assertNFSStatus(int expected, int actual) {

        if (expected != actual) {
            String message = String.format("expected:<%s> but was:<%s>",
                    nfsstat.toString(expected), nfsstat.toString(actual));
            throw new AssertionFailedError(message);
        }

    }
}
