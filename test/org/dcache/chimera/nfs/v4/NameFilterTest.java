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
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.ChimeraNFSException;
import org.junit.Test;

public class NameFilterTest {

    private static final byte[] BAD_UTF8 = new byte[]{
        (byte) 0xfc,
        (byte) 0x80,
        (byte) 0x80,
        (byte) 0x80,
        (byte) 0x80,
        (byte) 0xaf
    };
    private static final byte[] GOOD_UTF8 = "a normal string".getBytes();
    private static final byte[] EMPTY_NAME = new byte[0];
    private static final byte[] NAME_TOO_LONG = new byte[NFSv4Defaults.NFS4_MAXFILENAME + 1];

    @Test
    public void testGoodUtf8() throws ChimeraNFSException {
        NameFilter.convert(GOOD_UTF8);
    }

    @Test(expected = ChimeraNFSException.class)
    public void testBadUtf8() throws ChimeraNFSException {
        NameFilter.convert(BAD_UTF8);
    }

    @Test(expected = ChimeraNFSException.class)
    public void testNameTooLong() throws ChimeraNFSException {
        NameFilter.convert(NAME_TOO_LONG);
    }

    @Test(expected = ChimeraNFSException.class)
    public void testEmptyName() throws ChimeraNFSException {
        NameFilter.convert(EMPTY_NAME);
    }
//
}
