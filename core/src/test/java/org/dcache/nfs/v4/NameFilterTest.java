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

import com.google.common.base.Charsets;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadNameException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.NameTooLongException;
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
    private static final byte[] UTF8_WITH_SURROGAT = new byte[]{
        (byte) 0xed,
        (byte) 0xa0,
        (byte) 0x80
    };
    private static final byte[] GOOD_UTF8 = "a normal string".getBytes(Charsets.UTF_8);
    private static final byte[] EMPTY_NAME = new byte[0];
    private static final byte[] NAME_TOO_LONG = new byte[NFSv4Defaults.NFS4_MAXFILENAME + 1];
    private static final byte[] DOT = ".".getBytes(Charsets.UTF_8);
    private static final byte[] DOT_DOT = "..".getBytes(Charsets.UTF_8);
    private static final byte[] WITH_SLASH = "foo/bar".getBytes(Charsets.UTF_8);
    private static final byte[] GOOD_UTF8_ARM = "Երեվան".getBytes(Charsets.UTF_8);
    private static final byte[] GOOD_UTF8_HBR = "יְרוּשָׁלַיִם".getBytes(Charsets.UTF_8);

    @Test
    public void testGoodUtf8() throws ChimeraNFSException {
        NameFilter.convert(GOOD_UTF8);
    }

    @Test
    public void testGoodUtf8Arm() throws ChimeraNFSException {
        NameFilter.convert(GOOD_UTF8_ARM);
    }

    @Test
    public void testGoodUtf8Hbr() throws ChimeraNFSException {
        NameFilter.convert(GOOD_UTF8_HBR);
    }

    @Test(expected = InvalException.class)
    public void testBadUtf8() throws ChimeraNFSException {
        NameFilter.convert(BAD_UTF8);
    }

    @Test(expected = NameTooLongException.class)
    public void testNameTooLong() throws ChimeraNFSException {
        NameFilter.convert(NAME_TOO_LONG);
    }

    @Test(expected = InvalException.class)
    public void testEmptyName() throws ChimeraNFSException {
        NameFilter.convert(EMPTY_NAME);
    }

    @Test(expected = BadNameException.class)
    public void testDot() throws ChimeraNFSException {
        NameFilter.convert(DOT);
    }

    @Test(expected = BadNameException.class)
    public void testDotDot() throws ChimeraNFSException {
        NameFilter.convert(DOT_DOT);
    }

    @Test(expected = BadNameException.class)
    public void testNameWithSlash() throws ChimeraNFSException {
        NameFilter.convert(WITH_SLASH);
    }

    @Test(expected = InvalException.class)
    public void testUtf8WithSurrogat() throws ChimeraNFSException {
        NameFilter.convert(UTF8_WITH_SURROGAT);
    }
}
