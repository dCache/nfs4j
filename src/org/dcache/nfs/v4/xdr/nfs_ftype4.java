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
public interface nfs_ftype4 {

    public static final int NF4REG = 1;
    public static final int NF4DIR = 2;
    public static final int NF4BLK = 3;
    public static final int NF4CHR = 4;
    public static final int NF4LNK = 5;
    public static final int NF4SOCK = 6;
    public static final int NF4FIFO = 7;
    public static final int NF4ATTRDIR = 8;
    public static final int NF4NAMEDATTR = 9;

}
// End of nfs_ftype4.java
