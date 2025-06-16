/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.xdr;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.dcache.nfs.status.InvalException;
import org.junit.Test;

public class nfstime4Test {

    @Test(expected = InvalException.class)
    public void testInvalidNseconds() throws Exception {
        nfstime4 time = new nfstime4();
        time.seconds = 1;
        time.nseconds = 1000000000;
        time.toMillis();
    }

    /**
     * openjdk-1.8 + maven fail to compile. Suppress warning until a working alternative is found.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testValidTime() throws Exception {
        nfstime4 time = new nfstime4();
        time.seconds = 1;
        time.nseconds = (int) TimeUnit.MILLISECONDS.toNanos(5);

        long date = LocalDateTime.of(1970, 1, 1, 0, 0, 1, 0).toInstant(ZoneOffset.UTC).toEpochMilli();

        assertEquals("invalid date", date + 5, time.toMillis());
    }

}
