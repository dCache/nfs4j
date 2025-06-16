/*
 * Copyright (c) 2009 - 2021 Deutsches Elektronen-Synchroton,
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

import static org.dcache.nfs.v3.Utils.checkFilename;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.NameTooLongException;
import org.dcache.nfs.v3.xdr.nfstime3;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    private final static String GOOD_NAME = "someGoodName";
    private final static String EMPTY_NAME = "";
    private final static String WITH_SLASH = "some/withSlash";
    private final static String WITH_NULL = "someWith\0";
    private final static String GOOD_UTF8_ARM = "Երեվան";
    private final static String GOOD_UTF8_HBR = "יְרוּשָׁלַיִם";

    @Test
    public void testConvertTimestamp() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        long timestamp = dateFormat.parse("01/02/2003 04:06:06.789").getTime();
        nfstime3 converted = Utils.convertTimestamp(timestamp);
        Assert.assertEquals(timestamp / 1000, converted.seconds.value);
        Assert.assertEquals(1000000 * (timestamp % 1000), converted.nseconds.value);
        long decoded = Utils.convertTimestamp(converted);
        Assert.assertEquals(timestamp, decoded);
    }

    @Test
    public void testWithGoodName() throws AccessException, NameTooLongException {
        checkFilename(GOOD_NAME);
    }

    @Test
    public void testWithGoodArmName() throws AccessException, NameTooLongException {
        checkFilename(GOOD_UTF8_ARM);
    }

    @Test
    public void testWithGoodHbrName() throws AccessException, NameTooLongException {
        checkFilename(GOOD_UTF8_HBR);
    }

    @Test(expected = AccessException.class)
    public void testEmptyName() throws AccessException, NameTooLongException {
        checkFilename(EMPTY_NAME);
    }

    @Test(expected = AccessException.class)
    public void testNameWithSlash() throws AccessException, NameTooLongException {
        checkFilename(WITH_SLASH);
    }

    @Test(expected = AccessException.class)
    public void testNameWithNull() throws AccessException, NameTooLongException {
        checkFilename(WITH_NULL);
    }
}
