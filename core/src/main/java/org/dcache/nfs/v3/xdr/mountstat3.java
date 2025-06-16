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
package org.dcache.nfs.v3.xdr;

/**
 * Enumeration (collection of constants).
 */
public interface mountstat3 {

    public static final int MNT3_OK = 0;
    public static final int MNT3ERR_PERM = 1;
    public static final int MNT3ERR_NOENT = 2;
    public static final int MNT3ERR_IO = 5;
    public static final int MNT3ERR_ACCES = 13;
    public static final int MNT3ERR_NOTDIR = 20;
    public static final int MNT3ERR_INVAL = 22;
    public static final int MNT3ERR_NAMETOOLONG = 63;
    public static final int MNT3ERR_NOTSUPP = 10004;
    public static final int MNT3ERR_SERVERFAULT = 10006;

}
// End of mountstat3.java
