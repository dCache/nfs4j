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
 * A collection of constants used by the "nfs3_prot" ONC/RPC program.
 */
public interface nfs3_prot {
    public static final int NFSPROC3_MKDIR_3 = 9;
    public static final int NFSPROC3_CREATE_3 = 8;
    public static final int ACCESS3_DELETE = 0x0010;
    public static final int NFS3_COOKIEVERFSIZE = 8;
    public static final int NFSPROC3_SETATTR_3 = 2;
    public static final int FSF3_HOMOGENEOUS = 0x0008;
    public static final int NFSPROC3_READDIR_3 = 16;
    public static final int ACCESS3_EXECUTE = 0x0020;
    public static final int NFSPROC3_WRITE_3 = 7;
    public static final int FSF3_CANSETTIME = 0x0010;
    public static final int NFS3_FHSIZE = 64;
    public static final int NFSPROC3_LOOKUP_3 = 3;
    public static final int FSF3_LINK = 0x0001;
    public static final int NFSPROC3_GETATTR_3 = 1;
    public static final int ACCESS3_EXTEND = 0x0008;
    public static final int NFSPROC3_FSINFO_3 = 19;
    public static final int NFSPROC3_LINK_3 = 15;
    public static final int FSF3_SYMLINK = 0x0002;
    public static final int NFS3_CREATEVERFSIZE = 8;
    public static final int NFSPROC3_FSSTAT_3 = 18;
    public static final int NFSPROC3_RMDIR_3 = 13;
    public static final int NFSPROC3_SYMLINK_3 = 10;
    public static final int NFSPROC3_REMOVE_3 = 12;
    public static final int NFSPROC3_PATHCONF_3 = 20;
    public static final int NFSPROC3_NULL_3 = 0;
    public static final int NFSPROC3_READDIRPLUS_3 = 17;
    public static final int ACCESS3_READ = 0x0001;
    public static final int NFSPROC3_MKNOD_3 = 11;
    public static final int NFSPROC3_RENAME_3 = 14;
    public static final int NFS_PROGRAM = 100003;
    public static final int NFS_V3 = 3;
    public static final int NFSPROC3_READ_3 = 6;
    public static final int ACCESS3_LOOKUP = 0x0002;
    public static final int NFSPROC3_ACCESS_3 = 4;
    public static final int NFSPROC3_COMMIT_3 = 21;
    public static final int ACCESS3_MODIFY = 0x0004;
    public static final int NFSPROC3_READLINK_3 = 5;
    public static final int NFS3_WRITEVERFSIZE = 8;
}
// End of nfs3_prot.java
