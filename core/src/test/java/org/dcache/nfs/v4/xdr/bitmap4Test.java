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

import org.junit.Test;

/**
 *
 */
public class bitmap4Test {

    @Test
    public void testIterratorNoValues() {
        bitmap4 bitmap = new bitmap4();
        assertFalse(bitmap.iterator().hasNext());
    }

    @Test
    public void testIterratorZeroValues() {
        bitmap4 bitmap = new bitmap4(new int[] {0, 0});
        assertFalse(bitmap.iterator().hasNext());
    }

    @Test
    public void testIterratorWithValues() {
        bitmap4 bitmap = bitmap4.of(5);
        assertTrue(bitmap.iterator().hasNext());
    }

    @Test
    public void testCountOfValues() {
        bitmap4 bitmap = bitmap4.of(5, 34, 80, 129);
        int count = 0;

        for (Integer i : bitmap) {
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    public void testValues() {
        int[] values = new int[] {7, 15, 62, 63, 129};
        int[] testArray = new int[values.length];

        bitmap4 bitmap = bitmap4.of(values);
        int count = 0;

        for (Integer i : bitmap) {
            testArray[count++] = i;
        }
        assertArrayEquals(values, testArray);
    }

    @Test
    public void testIsSetNoValues() {
        bitmap4 bitmap = new bitmap4();
        assertFalse(bitmap.isSet(5));
    }

    @Test
    public void testSetNoValues() {
        bitmap4 bitmap = new bitmap4();
        bitmap.set(5);
        assertTrue(bitmap.isSet(5));
    }

    @Test
    public void testSetWithValues() {
        bitmap4 bitmap = bitmap4.of(5, 34, 80, 129);
        bitmap.set(7);
        assertTrue(bitmap.isSet(5));
        assertTrue(bitmap.isSet(7));
        assertTrue(bitmap.isSet(34));
        assertTrue(bitmap.isSet(80));
        assertTrue(bitmap.isSet(129));
        assertFalse(bitmap.isSet(6));
    }

    @Test
    public void testSameBitsDifferentSize() {
        bitmap4 b1 = new bitmap4(new int[] {8, 0, 5, 0, 0});
        bitmap4 b2 = new bitmap4(new int[] {8, 0, 5});
        assertEquals("arrays size must be ignored", b1, b2);
        assertEquals("arrays size must be ignored", b1.hashCode(), b2.hashCode());
    }

    @Test
    public void testSameSizeDifferentBits() {
        bitmap4 b1 = new bitmap4(new int[] {0, 1});
        bitmap4 b2 = new bitmap4(new int[] {1});
        assertNotEquals("different bits not respected", b1, b2);
        assertNotEquals("different bits not respected", b1.hashCode(), b2.hashCode());
    }

}
