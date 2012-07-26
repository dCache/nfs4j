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
/**
 * Enumeration (collection of constants).
 */
public interface nfs_opnum4 {

    public static final int OP_ACCESS = 3;
    public static final int OP_CLOSE = 4;
    public static final int OP_COMMIT = 5;
    public static final int OP_CREATE = 6;
    public static final int OP_DELEGPURGE = 7;
    public static final int OP_DELEGRETURN = 8;
    public static final int OP_GETATTR = 9;
    public static final int OP_GETFH = 10;
    public static final int OP_LINK = 11;
    public static final int OP_LOCK = 12;
    public static final int OP_LOCKT = 13;
    public static final int OP_LOCKU = 14;
    public static final int OP_LOOKUP = 15;
    public static final int OP_LOOKUPP = 16;
    public static final int OP_NVERIFY = 17;
    public static final int OP_OPEN = 18;
    public static final int OP_OPENATTR = 19;
    public static final int OP_OPEN_CONFIRM = 20;
    public static final int OP_OPEN_DOWNGRADE = 21;
    public static final int OP_PUTFH = 22;
    public static final int OP_PUTPUBFH = 23;
    public static final int OP_PUTROOTFH = 24;
    public static final int OP_READ = 25;
    public static final int OP_READDIR = 26;
    public static final int OP_READLINK = 27;
    public static final int OP_REMOVE = 28;
    public static final int OP_RENAME = 29;
    public static final int OP_RENEW = 30;
    public static final int OP_RESTOREFH = 31;
    public static final int OP_SAVEFH = 32;
    public static final int OP_SECINFO = 33;
    public static final int OP_SETATTR = 34;
    public static final int OP_SETCLIENTID = 35;
    public static final int OP_SETCLIENTID_CONFIRM = 36;
    public static final int OP_VERIFY = 37;
    public static final int OP_WRITE = 38;
    public static final int OP_RELEASE_LOCKOWNER = 39;
    public static final int OP_BACKCHANNEL_CTL = 40;
    public static final int OP_BIND_CONN_TO_SESSION = 41;
    public static final int OP_EXCHANGE_ID = 42;
    public static final int OP_CREATE_SESSION = 43;
    public static final int OP_DESTROY_SESSION = 44;
    public static final int OP_FREE_STATEID = 45;
    public static final int OP_GET_DIR_DELEGATION = 46;
    public static final int OP_GETDEVICEINFO = 47;
    public static final int OP_GETDEVICELIST = 48;
    public static final int OP_LAYOUTCOMMIT = 49;
    public static final int OP_LAYOUTGET = 50;
    public static final int OP_LAYOUTRETURN = 51;
    public static final int OP_SECINFO_NO_NAME = 52;
    public static final int OP_SEQUENCE = 53;
    public static final int OP_SET_SSV = 54;
    public static final int OP_TEST_STATEID = 55;
    public static final int OP_WANT_DELEGATION = 56;
    public static final int OP_DESTROY_CLIENTID = 57;
    public static final int OP_RECLAIM_COMPLETE = 58;
    public static final int OP_ILLEGAL = 10044;

}
// End of nfs_opnum4.java
