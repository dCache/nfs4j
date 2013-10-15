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
 * A collection of constants used by the "mount_prot" ONC/RPC program.
 */
public interface mount_prot {
    public static final int MOUNTPROC_DUMP_1 = 2;
    public static final int MNTNAMLEN = 255;
    public static final int FHSIZE3 = 64;
    public static final int MOUNTPROC3_DUMP_3 = 2;
    public static final int FHSIZE2 = 32;
    public static final int MOUNTPROC_EXPORTALL_1 = 6;
    public static final int MOUNTPROC_NULL_1 = 0;
    public static final int MOUNTPROC3_NULL_3 = 0;
    public static final int MOUNTPROC_EXPORT_1 = 5;
    public static final int MOUNTPROC3_UMNTALL_3 = 4;
    public static final int MOUNT_PROGRAM = 100005;
    public static final int MOUNTPROC_UMNTALL_1 = 4;
    public static final int MOUNT_V3 = 3;
    public static final int MOUNTPROC_MNT_1 = 1;
    public static final int MOUNT_V1 = 1;
    public static final int MOUNTPROC3_EXPORT_3 = 5;
    public static final int MOUNTPROC_UMNT_1 = 3;
    public static final int MOUNTPROC3_MNT_3 = 1;
    public static final int MNTPATHLEN = 1024;
    public static final int MOUNTPROC3_UMNT_3 = 3;
}
// End of mount_prot.java
