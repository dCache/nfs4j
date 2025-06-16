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

/**
 * Enumeration (collection of constants).
 */
public interface nfs_lock_type4 {

    public static final int READ_LT = 1;
    public static final int WRITE_LT = 2;
    public static final int READW_LT = 3;
    public static final int WRITEW_LT = 4;

    public static String toString(int lt) {
        switch (lt) {
            case READ_LT:
                return "READ_LT";
            case READW_LT:
                return "READW_LT";
            case WRITE_LT:
                return "WRITE_LT";
            case WRITEW_LT:
                return "WRITEW_LT";
            default:
                throw new IllegalArgumentException("bad lock type: " + lt);
        }
    }
}
// End of nfs_lock_type4.java
