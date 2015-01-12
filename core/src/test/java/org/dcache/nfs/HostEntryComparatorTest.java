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
package org.dcache.nfs;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.dcache.nfs.HostEntryComparator.*;

public class HostEntryComparatorTest {

    @Test
    public void shouldReturnZeroIfBoughWitoutMask() {
        assertEquals(0, compare("a.b.c.d", "d.e.f.g"));
    }

    @Test
    public void shouldReturnZeroIfBoughWitoutSameMask() {
        assertEquals(0, compare("a.b.c.d/24", "d.e.f.g/24"));
    }

    @Test
    public void shouldBiggerMaskWin() {
        assertEquals(1, compare("a.b.c.d/32", "d.e.f.g/24"));
    }

    @Test
    public void shouldWithoutMaskSWin() {
        assertEquals(1, compare("a.b.c.d", "d.e.f.g/24"));
    }

    @Test
    public void shouldFirstWildcatWin() {
        assertEquals(1, compare("a*.somewhere.in.net", "ab*.somewhere.in.net"));
        assertEquals(-1, compare("ab*.somewhere.in.net", "a*.somewhere.in.net"));
    }

    @Test
    public void shouldIpBeFirst() {
        assertEquals(1, compare("a*.somewhere.in.net", "a.b.c.d"));
    }

    @Test
    public void shouldFullNameBeFirst() {
        assertEquals(-1, compare("a.somewhere.in.net", "*.somewhere.in.net"));
    }
}
