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
package org.dcache.chimera.nfs.v4.xdr;

import org.junit.Test;
import static org.junit.Assert.*;


public class stateid4Test {

    @Test
    public void testEqualsTrue() {

        stateid4 stateidA = new stateid4();
        stateidA.seqid = new uint32_t(1);
        stateidA.other = "state".getBytes();

        stateid4 stateidB = new stateid4();
        stateidB.seqid = new uint32_t(1);
        stateidB.other = "state".getBytes();

        assertTrue("equal keys not equal", stateidA.equals(stateidB));
        assertTrue("equal, but different hashCode", stateidA.hashCode() == stateidB.hashCode() );
    }

    @Test
    public void testEqualsSame() {

        stateid4 stateidA = new stateid4();
        stateidA.seqid = new uint32_t(1);
        stateidA.other = "state".getBytes();

        assertTrue("equal keys not equal", stateidA.equals(stateidA));
    }

    @Test
    public void testDifferSequence() {

        stateid4 stateidA = new stateid4();
        stateidA.seqid = new uint32_t(1);
        stateidA.other = "state".getBytes();

        stateid4 stateidB = new stateid4();
        stateidB.seqid = new uint32_t(2);
        stateidB.other = "state".getBytes();

        assertTrue("differ by sequence should still be equal", stateidA.equals(stateidB));
    }

    @Test
    public void testDifferOther() {

        stateid4 stateidA = new stateid4();
        stateidA.seqid = new uint32_t(1);
        stateidA.other = "stateA".getBytes();

        stateid4 stateidB = new stateid4();
        stateidB.seqid = new uint32_t(1);
        stateidB.other = "stateB".getBytes();

        assertFalse("differ by other not detected", stateidA.equals(stateidB));
    }
}
