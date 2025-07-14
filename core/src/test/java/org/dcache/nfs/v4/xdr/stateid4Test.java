/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class stateid4Test {

    @Test
    public void testEqualsTrue() {

        stateid4 stateidA = new stateid4("state".getBytes(), 1);

        stateid4 stateidB = new stateid4("state".getBytes(), 1);

        assertTrue("equal keys not equal", stateidA.equals(stateidB));
        assertTrue("equal, but different hashCode", stateidA.hashCode() == stateidB.hashCode());
        assertTrue("not equal by other and seqid must", stateidA.equalsWithSeq(stateidB));
    }

    @Test
    public void testEqualsSame() {

        stateid4 stateidA = new stateid4("state".getBytes(), 1);

        assertTrue("equal keys not equal", stateidA.equals(stateidA));
    }

    @Test
    public void testDifferSequence() {

        stateid4 stateidA = new stateid4("state".getBytes(), 1);

        stateid4 stateidB = new stateid4("state".getBytes(), 2);

        assertTrue("differ by sequence should still be equal", stateidA.equals(stateidB));
        assertFalse("differ by sequence can't be equal", stateidA.equalsWithSeq(stateidB));
    }

    @Test
    public void testDifferOther() {

        stateid4 stateidA = new stateid4("stateA".getBytes(), 1);

        stateid4 stateidB = new stateid4("stateB".getBytes(), 1);

        assertFalse("differ by other not detected", stateidA.equals(stateidB));
    }
}
