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

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.dcache.nfs.v4.xdr.stateid4;
import org.junit.Test;

/**
 *
 * @author tigran
 */
public class StateidsTest {

    @Test
    public void testIsAllZerosIsStateless() {
        assertTrue(Stateids.isStateLess(Stateids.ZeroStateId()));
    }

    @Test
    public void testIsAllOnesIsStateless() {
        assertTrue(Stateids.isStateLess(Stateids.OneStateId()));
    }

    @Test
    public void testIsRegularIsStateless() {
        assertFalse(Stateids.isStateLess(new stateid4("a state".getBytes(StandardCharsets.UTF_8), 1)));
    }

}
