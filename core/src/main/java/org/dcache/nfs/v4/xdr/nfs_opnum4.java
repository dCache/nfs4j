/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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
public final class nfs_opnum4 {

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
    public static final int OP_ALLOCATE = 59;
    public static final int OP_COPY = 60;
    public static final int OP_COPY_NOTIFY = 61;
    public static final int OP_DEALLOCATE = 62;
    public static final int OP_IO_ADVISE = 63;
    public static final int OP_LAYOUTERROR = 64;
    public static final int OP_LAYOUTSTATS = 65;
    public static final int OP_OFFLOAD_CANCEL = 66;
    public static final int OP_OFFLOAD_STATUS = 67;
    public static final int OP_READ_PLUS = 68;
    public static final int OP_SEEK = 69;
    public static final int OP_WRITE_SAME = 70;
    public static final int OP_CLONE = 71;
    public static final int OP_ILLEGAL = 10044;

    private nfs_opnum4() {
    }

    public static String toString(int opnum) {

        switch (opnum) {
            case nfs_opnum4.OP_ACCESS:
                return "ACCESS";
            case nfs_opnum4.OP_CLOSE:
                return "CLOSE";
            case nfs_opnum4.OP_COMMIT:
                return "COMMIT";
            case nfs_opnum4.OP_CREATE:
                return "CREATE";
            case nfs_opnum4.OP_DELEGPURGE:
                return "DELEGPURGE";
            case nfs_opnum4.OP_DELEGRETURN:
                return "DELEGRETURN";
            case nfs_opnum4.OP_GETATTR:
                return "GETATTR";
            case nfs_opnum4.OP_GETFH:
                return "GETFH";
            case nfs_opnum4.OP_LINK:
                return "LINK";
            case nfs_opnum4.OP_LOCK:
                return "LOCK";
            case nfs_opnum4.OP_LOCKT:
                return "LOCKT";
            case nfs_opnum4.OP_LOCKU:
                return "LOCKU";
            case nfs_opnum4.OP_LOOKUP:
                return "LOOKUP";
            case nfs_opnum4.OP_LOOKUPP:
                return "LOOKUPP";
            case nfs_opnum4.OP_NVERIFY:
                return "NVERIFY";
            case nfs_opnum4.OP_OPEN:
                return "OPEN";
            case nfs_opnum4.OP_OPENATTR:
                return "OPENATTR";
            case nfs_opnum4.OP_OPEN_CONFIRM:
                return "OPEN_CONFIRM";
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                return "OPEN_DOWNGRADE";
            case nfs_opnum4.OP_PUTFH:
                return "PUTFH";
            case nfs_opnum4.OP_PUTPUBFH:
                return "PUTPUBFH";
            case nfs_opnum4.OP_PUTROOTFH:
                return "PUTROOTFH";
            case nfs_opnum4.OP_READ:
                return "READ";
            case nfs_opnum4.OP_READDIR:
                return "READDIR";
            case nfs_opnum4.OP_READLINK:
                return "READLINK";
            case nfs_opnum4.OP_REMOVE:
                return "REMOVE";
            case nfs_opnum4.OP_RENAME:
                return "RENAME";
            case nfs_opnum4.OP_RENEW:
                return "RENEW";
            case nfs_opnum4.OP_RESTOREFH:
                return "RESTOREFH";
            case nfs_opnum4.OP_SAVEFH:
                return "SAVEFH";
            case nfs_opnum4.OP_SECINFO:
                return "SECINFO";
            case nfs_opnum4.OP_SETATTR:
                return "SETATTR";
            case nfs_opnum4.OP_SETCLIENTID:
                return "SETCLIENTID";
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                return "SETCLIENTID_CONFIRM";
            case nfs_opnum4.OP_VERIFY:
                return "VERIFY";
            case nfs_opnum4.OP_WRITE:
                return "WRITE";
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                return "RELEASE_LOCKOWNER";
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                return "BACKCHANNEL_CTL";
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                return "BIND_CONN_TO_SESSION";
            case nfs_opnum4.OP_EXCHANGE_ID:
                return "EXCHANGE_ID";
            case nfs_opnum4.OP_CREATE_SESSION:
                return "CREATE_SESSION";
            case nfs_opnum4.OP_DESTROY_SESSION:
                return "DESTROY_SESSION";
            case nfs_opnum4.OP_FREE_STATEID:
                return "FREE_STATEID";
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                return "GET_DIR_DELEGATION";
            case nfs_opnum4.OP_GETDEVICEINFO:
                return "GETDEVICEINFO";
            case nfs_opnum4.OP_GETDEVICELIST:
                return "GETDEVICELIST";
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                return "LAYOUTCOMMIT";
            case nfs_opnum4.OP_LAYOUTGET:
                return "LAYOUTGET";
            case nfs_opnum4.OP_LAYOUTRETURN:
                return "LAYOUTRETURN";
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                return "SECINFO_NO_NAME";
            case nfs_opnum4.OP_SEQUENCE:
                return "SEQUENCE";
            case nfs_opnum4.OP_SET_SSV:
                return "SET_SSV";
            case nfs_opnum4.OP_TEST_STATEID:
                return "TEST_STATEID";
            case nfs_opnum4.OP_WANT_DELEGATION:
                return "WANT_DELEGATION";
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                return "DESTROY_CLIENTID";
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                return "RECLAIM_COMPLETE";
            case nfs_opnum4.OP_ALLOCATE:
                return "ALLOCATE";
            case nfs_opnum4.OP_COPY:
                return "COPY";
            case nfs_opnum4.OP_COPY_NOTIFY:
                return "COPY_NOTIFY";
            case nfs_opnum4.OP_DEALLOCATE:
                return "DEALLOCATE";
            case nfs_opnum4.OP_IO_ADVISE:
                return "IO_ADVISE";
            case nfs_opnum4.OP_LAYOUTERROR:
                return "LAYOUTERROR";
            case nfs_opnum4.OP_LAYOUTSTATS:
                return "LAYOUTSTATS";
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                return "OFFLOAD_CANCEL";
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                return "OFFLOAD_STATUS";
            case nfs_opnum4.OP_READ_PLUS:
                return "READ_PLUS";
            case nfs_opnum4.OP_SEEK:
                return "SEEK";
            case nfs_opnum4.OP_WRITE_SAME:
                return "WRITE_SAME";
            case nfs_opnum4.OP_CLONE:
                return "CLONE";
            case nfs_opnum4.OP_ILLEGAL:
                return "ILLEGAL";
            default:
                return "ILLEGAL<" + opnum + ">";
        }
    }
}
// End of nfs_opnum4.java
