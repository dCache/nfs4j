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
package org.dcache.nfs.v4.xdr;
/**
 * Enumeration (collection of constants).
 */
public interface why_no_delegation4 {

    public static final int WND4_NOT_WANTED = 0;
    public static final int WND4_CONTENTION = 1;
    public static final int WND4_RESOURCE = 2;
    public static final int WND4_NOT_SUPP_FTYPE = 3;
    public static final int WND4_WRITE_DELEG_NOT_SUPP_FTYPE = 4;
    public static final int WND4_NOT_SUPP_UPGRADE = 5;
    public static final int WND4_NOT_SUPP_DOWNGRADE = 6;
    public static final int WND4_CANCELED = 7;
    public static final int WND4_IS_DIR = 8;

}
// End of why_no_delegation4.java
