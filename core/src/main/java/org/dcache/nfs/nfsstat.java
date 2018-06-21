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
package org.dcache.nfs;

import org.dcache.nfs.status.*;

/**
 * NFS errors defined for v2, v3 and v4(0, 1, 2).
 */
public final class nfsstat {

    public static final int NFS_OK = 0;
    public static final int NFSERR_PERM = 1;
    public static final int NFSERR_NOENT = 2;
    public static final int NFSERR_IO = 5;
    public static final int NFSERR_NXIO = 6;
    public static final int NFSERR_ACCESS = 13;
    public static final int NFSERR_EXIST = 17;
    public static final int NFSERR_XDEV = 18;
    public static final int NFSERR_NODEV = 19;
    public static final int NFSERR_NOTDIR = 20;
    public static final int NFSERR_ISDIR = 21;
    public static final int NFSERR_INVAL = 22;
    public static final int NFSERR_FBIG = 27;
    public static final int NFSERR_NOSPC = 28;
    public static final int NFSERR_ROFS = 30;
    public static final int NFSERR_MLINK = 31;
    public static final int NFSERR_NAMETOOLONG = 63;
    public static final int NFSERR_NOTEMPTY = 66;
    public static final int NFSERR_DQUOT = 69;
    public static final int NFSERR_STALE = 70;
    public static final int NFSERR_REMOTE = 71;
    public static final int NFSERR_WFLUSH = 99;
    public static final int NFSERR_BADHANDLE = 10001;
    public static final int NFSERR_NOT_SYNC = 10002;
    public static final int NFSERR_BAD_COOKIE = 10003;
    public static final int NFSERR_NOTSUPP = 10004;
    public static final int NFSERR_TOOSMALL = 10005;
    public static final int NFSERR_SERVERFAULT = 10006;
    public static final int NFSERR_BADTYPE = 10007;
    public static final int NFSERR_DELAY = 10008;
    public static final int NFSERR_JUKEBOX = NFSERR_DELAY;
    public static final int NFSERR_SAME = 10009;
    public static final int NFSERR_DENIED = 10010;
    public static final int NFSERR_EXPIRED = 10011;
    public static final int NFSERR_LOCKED = 10012;
    public static final int NFSERR_GRACE = 10013;
    public static final int NFSERR_FHEXPIRED = 10014;
    public static final int NFSERR_SHARE_DENIED = 10015;
    public static final int NFSERR_WRONGSEC = 10016;
    public static final int NFSERR_CLID_INUSE = 10017;
    public static final int NFSERR_RESOURCE = 10018;
    public static final int NFSERR_MOVED = 10019;
    public static final int NFSERR_NOFILEHANDLE = 10020;
    public static final int NFSERR_MINOR_VERS_MISMATCH = 10021;
    public static final int NFSERR_STALE_CLIENTID = 10022;
    public static final int NFSERR_STALE_STATEID = 10023;
    public static final int NFSERR_OLD_STATEID = 10024;
    public static final int NFSERR_BAD_STATEID = 10025;
    public static final int NFSERR_BAD_SEQID = 10026;
    public static final int NFSERR_NOT_SAME = 10027;
    public static final int NFSERR_LOCK_RANGE = 10028;
    public static final int NFSERR_SYMLINK = 10029;
    public static final int NFSERR_RESTOREFH = 10030;
    public static final int NFSERR_LEASE_MOVED = 10031;
    public static final int NFSERR_ATTRNOTSUPP = 10032;
    public static final int NFSERR_NO_GRACE = 10033;
    public static final int NFSERR_RECLAIM_BAD = 10034;
    public static final int NFSERR_RECLAIM_CONFLICT = 10035;
    public static final int NFSERR_BADXDR = 10036;
    public static final int NFSERR_LOCKS_HELD = 10037;
    public static final int NFSERR_OPENMODE = 10038;
    public static final int NFSERR_BADOWNER = 10039;
    public static final int NFSERR_BADCHAR = 10040;
    public static final int NFSERR_BADNAME = 10041;
    public static final int NFSERR_BAD_RANGE = 10042;
    public static final int NFSERR_LOCK_NOTSUPP = 10043;
    public static final int NFSERR_OP_ILLEGAL = 10044;
    public static final int NFSERR_DEADLOCK = 10045;
    public static final int NFSERR_FILE_OPEN = 10046;
    public static final int NFSERR_ADMIN_REVOKED = 10047;
    public static final int NFSERR_CB_PATH_DOWN = 10048;
    public static final int NFSERR_BADIOMODE = 10049;
    public static final int NFSERR_BADLAYOUT = 10050;
    public static final int NFSERR_BAD_SESSION_DIGEST = 10051;
    public static final int NFSERR_BADSESSION = 10052;
    public static final int NFSERR_BADSLOT = 10053;
    public static final int NFSERR_COMPLETE_ALREADY = 10054;
    public static final int NFSERR_CONN_NOT_BOUND_TO_SESSION = 10055;
    public static final int NFSERR_DELEG_ALREADY_WANTED = 10056;
    public static final int NFSERR_BACK_CHAN_BUSY = 10057;
    public static final int NFSERR_LAYOUTTRYLATER = 10058;
    public static final int NFSERR_LAYOUTUNAVAILABLE = 10059;
    public static final int NFSERR_NOMATCHING_LAYOUT = 10060;
    public static final int NFSERR_RECALLCONFLICT = 10061;
    public static final int NFSERR_UNKNOWN_LAYOUTTYPE = 10062;
    public static final int NFSERR_SEQ_MISORDERED = 10063;
    public static final int NFSERR_SEQUENCE_POS = 10064;
    public static final int NFSERR_REQ_TOO_BIG = 10065;
    public static final int NFSERR_REP_TOO_BIG = 10066;
    public static final int NFSERR_REP_TOO_BIG_TO_CACHE = 10067;
    public static final int NFSERR_RETRY_UNCACHED_REP = 10068;
    public static final int NFSERR_UNSAFE_COMPOUND = 10069;
    public static final int NFSERR_TOO_MANY_OPS = 10070;
    public static final int NFSERR_OP_NOT_IN_SESSION = 10071;
    public static final int NFSERR_HASH_ALG_UNSUPP = 10072;
    public static final int NFSERR_CONN_BINDING_NOT_ENFORCED = 10073;
    public static final int NFSERR_CLIENTID_BUSY = 10074;
    public static final int NFSERR_PNFS_IO_HOLE = 10075;
    public static final int NFSERR_SEQ_FALSE_RETRY = 10076;
    public static final int NFSERR_BAD_HIGH_SLOT = 10077;
    public static final int NFSERR_DEADSESSION = 10078;
    public static final int NFSERR_ENCR_ALG_UNSUPP = 10079;
    public static final int NFSERR_PNFS_NO_LAYOUT = 10080;
    public static final int NFSERR_NOT_ONLY_OP = 10081;
    public static final int NFSERR_WRONG_CRED = 10082;
    public static final int NFSERR_WRONG_TYPE = 10083;
    public static final int NFSERR_DIRDELEG_UNAVAIL = 10084;
    public static final int NFSERR_REJECT_DELEG = 10085;
    public static final int NFSERR_RETURNCONFLICT = 10086;
    public static final int NFS4ERR_DELEG_REVOKED = 10087;
    public static final int NFS4ERR_PARTNER_NOTSUPP = 10088;
    public static final int NFS4ERR_PARTNER_NO_AUTH = 10089;
    public static final int NFS4ERR_UNION_NOTSUPP = 10090;
    public static final int NFS4ERR_OFFLOAD_DENIED = 10091;
    public static final int NFS4ERR_WRONG_LFS = 10092;
    public static final int NFS4ERR_BADLABEL = 10093;
    public static final int NFS4ERR_OFFLOAD_NO_REQS = 10094;

    private nfsstat() {/* no instance allowed */};

    public static String toString(int errorCode) {

        switch (errorCode) {
            case nfsstat.NFS_OK:
                return "NFS4_OK";
            case nfsstat.NFSERR_PERM:
                return "NFS4ERR_PERM";
            case nfsstat.NFSERR_NOENT:
                return "NFS4ERR_NOENT";
            case nfsstat.NFSERR_IO:
                return "NFS4ERR_IO";
            case nfsstat.NFSERR_NXIO:
                return "NFS4ERR_NXIO";
            case nfsstat.NFSERR_ACCESS:
                return "NFS4ERR_ACCESS";
            case nfsstat.NFSERR_EXIST:
                return "NFS4ERR_EXIST";
            case nfsstat.NFSERR_XDEV:
                return "NFS4ERR_XDEV";
            case nfsstat.NFSERR_NODEV:
                return "NFSERR_NODEV";
            case nfsstat.NFSERR_NOTDIR:
                return "NFS4ERR_NOTDIR";
            case nfsstat.NFSERR_ISDIR:
                return "NFS4ERR_ISDIR";
            case nfsstat.NFSERR_INVAL:
                return "NFS4ERR_INVAL";
            case nfsstat.NFSERR_FBIG:
                return "NFS4ERR_FBIG";
            case nfsstat.NFSERR_NOSPC:
                return "NFS4ERR_NOSPC";
            case nfsstat.NFSERR_ROFS:
                return "NFS4ERR_ROFS";
            case nfsstat.NFSERR_MLINK:
                return "NFS4ERR_MLINK";
            case nfsstat.NFSERR_NAMETOOLONG:
                return "NFS4ERR_NAMETOOLONG";
            case nfsstat.NFSERR_NOTEMPTY:
                return "NFS4ERR_NOTEMPTY";
            case nfsstat.NFSERR_DQUOT:
                return "NFS4ERR_DQUOT";
            case nfsstat.NFSERR_STALE:
                return "NFS4ERR_STALE";
            case nfsstat.NFSERR_BADHANDLE:
                return "NFS4ERR_BADHANDLE";
            case nfsstat.NFSERR_NOT_SYNC:
                return "NFSERR_NOT_SYNC";
            case nfsstat.NFSERR_BAD_COOKIE:
                return "NFS4ERR_BAD_COOKIE";
            case nfsstat.NFSERR_NOTSUPP:
                return "NFS4ERR_NOTSUPP";
            case nfsstat.NFSERR_TOOSMALL:
                return "NFS4ERR_TOOSMALL";
            case nfsstat.NFSERR_SERVERFAULT:
                return "NFS4ERR_SERVERFAULT";
            case nfsstat.NFSERR_BADTYPE:
                return "NFS4ERR_BADTYPE";
            case nfsstat.NFSERR_DELAY:
                return "NFS4ERR_DELAY";
            case nfsstat.NFSERR_SAME:
                return "NFS4ERR_SAME";
            case nfsstat.NFSERR_DENIED:
                return "NFS4ERR_DENIED";
            case nfsstat.NFSERR_EXPIRED:
                return "NFS4ERR_EXPIRED";
            case nfsstat.NFSERR_LOCKED:
                return "NFS4ERR_LOCKED";
            case nfsstat.NFSERR_GRACE:
                return "NFS4ERR_GRACE";
            case nfsstat.NFSERR_FHEXPIRED:
                return "NFS4ERR_FHEXPIRED";
            case nfsstat.NFSERR_SHARE_DENIED:
                return "NFS4ERR_SHARE_DENIED";
            case nfsstat.NFSERR_WRONGSEC:
                return "NFS4ERR_WRONGSEC";
            case nfsstat.NFSERR_CLID_INUSE:
                return "NFS4ERR_CLID_INUSE";
            case nfsstat.NFSERR_RESOURCE:
                return "NFS4ERR_RESOURCE";
            case nfsstat.NFSERR_MOVED:
                return "NFS4ERR_MOVED";
            case nfsstat.NFSERR_NOFILEHANDLE:
                return "NFS4ERR_NOFILEHANDLE";
            case nfsstat.NFSERR_MINOR_VERS_MISMATCH:
                return "NFS4ERR_MINOR_VERS_MISMATCH";
            case nfsstat.NFSERR_STALE_CLIENTID:
                return "NFS4ERR_STALE_CLIENTID";
            case nfsstat.NFSERR_STALE_STATEID:
                return "NFS4ERR_STALE_STATEID";
            case nfsstat.NFSERR_OLD_STATEID:
                return "NFS4ERR_OLD_STATEID";
            case nfsstat.NFSERR_BAD_STATEID:
                return "NFS4ERR_BAD_STATEID";
            case nfsstat.NFSERR_BAD_SEQID:
                return "NFS4ERR_BAD_SEQID";
            case nfsstat.NFSERR_NOT_SAME:
                return "NFS4ERR_NOT_SAME";
            case nfsstat.NFSERR_LOCK_RANGE:
                return "NFS4ERR_LOCK_RANGE";
            case nfsstat.NFSERR_SYMLINK:
                return "NFS4ERR_SYMLINK";
            case nfsstat.NFSERR_RESTOREFH:
                return "NFS4ERR_RESTOREFH";
            case nfsstat.NFSERR_LEASE_MOVED:
                return "NFS4ERR_LEASE_MOVED";
            case nfsstat.NFSERR_ATTRNOTSUPP:
                return "NFS4ERR_ATTRNOTSUPP";
            case nfsstat.NFSERR_NO_GRACE:
                return "NFS4ERR_NO_GRACE";
            case nfsstat.NFSERR_RECLAIM_BAD:
                return "NFS4ERR_RECLAIM_BAD";
            case nfsstat.NFSERR_RECLAIM_CONFLICT:
                return "NFS4ERR_RECLAIM_CONFLICT";
            case nfsstat.NFSERR_BADXDR:
                return "NFS4ERR_BADXDR";
            case nfsstat.NFSERR_LOCKS_HELD:
                return "NFS4ERR_LOCKS_HELD";
            case nfsstat.NFSERR_OPENMODE:
                return "NFS4ERR_OPENMODE";
            case nfsstat.NFSERR_BADOWNER:
                return "NFS4ERR_BADOWNER";
            case nfsstat.NFSERR_BADCHAR:
                return "NFS4ERR_BADCHAR";
            case nfsstat.NFSERR_BADNAME:
                return "NFS4ERR_BADNAME";
            case nfsstat.NFSERR_BAD_RANGE:
                return "NFS4ERR_BAD_RANGE";
            case nfsstat.NFSERR_LOCK_NOTSUPP:
                return "NFS4ERR_LOCK_NOTSUPP";
            case nfsstat.NFSERR_OP_ILLEGAL:
                return "NFS4ERR_OP_ILLEGAL";
            case nfsstat.NFSERR_DEADLOCK:
                return "NFS4ERR_DEADLOCK";
            case nfsstat.NFSERR_FILE_OPEN:
                return "NFS4ERR_FILE_OPEN";
            case nfsstat.NFSERR_ADMIN_REVOKED:
                return "NFS4ERR_ADMIN_REVOKED";
            case nfsstat.NFSERR_CB_PATH_DOWN:
                return "NFS4ERR_CB_PATH_DOWN";
            case nfsstat.NFSERR_BADIOMODE:
                return "NFS4ERR_BADIOMODE";
            case nfsstat.NFSERR_BADLAYOUT:
                return "NFS4ERR_BADLAYOUT";
            case nfsstat.NFSERR_BAD_SESSION_DIGEST:
                return "NFS4ERR_BAD_SESSION_DIGEST";
            case nfsstat.NFSERR_BADSESSION:
                return "NFS4ERR_BADSESSION";
            case nfsstat.NFSERR_BADSLOT:
                return "NFS4ERR_BADSLOT";
            case nfsstat.NFSERR_COMPLETE_ALREADY:
                return "NFS4ERR_COMPLETE_ALREADY";
            case nfsstat.NFSERR_CONN_NOT_BOUND_TO_SESSION:
                return "NFS4ERR_CONN_NOT_BOUND_TO_SESSION";
            case nfsstat.NFSERR_DELEG_ALREADY_WANTED:
                return "NFS4ERR_DELEG_ALREADY_WANTED";
            case nfsstat.NFSERR_BACK_CHAN_BUSY:
                return "NFS4ERR_BACK_CHAN_BUSY";
            case nfsstat.NFSERR_LAYOUTTRYLATER:
                return "NFS4ERR_LAYOUTTRYLATER";
            case nfsstat.NFSERR_LAYOUTUNAVAILABLE:
                return "NFS4ERR_LAYOUTUNAVAILABLE";
            case nfsstat.NFSERR_NOMATCHING_LAYOUT:
                return "NFS4ERR_NOMATCHING_LAYOUT";
            case nfsstat.NFSERR_RECALLCONFLICT:
                return "NFS4ERR_RECALLCONFLICT";
            case nfsstat.NFSERR_UNKNOWN_LAYOUTTYPE:
                return "NFS4ERR_UNKNOWN_LAYOUTTYPE";
            case nfsstat.NFSERR_SEQ_MISORDERED:
                return "NFS4ERR_SEQ_MISORDERED";
            case nfsstat.NFSERR_SEQUENCE_POS:
                return "NFS4ERR_SEQUENCE_POS";
            case nfsstat.NFSERR_REQ_TOO_BIG:
                return "NFS4ERR_REQ_TOO_BIG";
            case nfsstat.NFSERR_REP_TOO_BIG:
                return "NFS4ERR_REP_TOO_BIG";
            case nfsstat.NFSERR_REP_TOO_BIG_TO_CACHE:
                return "NFS4ERR_REP_TOO_BIG_TO_CACHE";
            case nfsstat.NFSERR_RETRY_UNCACHED_REP:
                return "NFS4ERR_RETRY_UNCACHED_REP";
            case nfsstat.NFSERR_UNSAFE_COMPOUND:
                return "NFS4ERR_UNSAFE_COMPOUND";
            case nfsstat.NFSERR_TOO_MANY_OPS:
                return "NFS4ERR_TOO_MANY_OPS";
            case nfsstat.NFSERR_OP_NOT_IN_SESSION:
                return "NFS4ERR_OP_NOT_IN_SESSION";
            case nfsstat.NFSERR_HASH_ALG_UNSUPP:
                return "NFS4ERR_HASH_ALG_UNSUPP";
            case nfsstat.NFSERR_CONN_BINDING_NOT_ENFORCED:
                return "NFS4ERR_CONN_BINDING_NOT_ENFORCED";
            case nfsstat.NFSERR_CLIENTID_BUSY:
                return "NFS4ERR_CLIENTID_BUSY";
            case nfsstat.NFSERR_PNFS_IO_HOLE:
                return "NFS4ERR_PNFS_IO_HOLE";
            case nfsstat.NFSERR_SEQ_FALSE_RETRY:
                return "NFS4ERR_SEQ_FALSE_RETRY";
            case nfsstat.NFSERR_BAD_HIGH_SLOT:
                return "NFS4ERR_BAD_HIGH_SLOT";
            case nfsstat.NFSERR_DEADSESSION:
                return "NFS4ERR_DEADSESSION";
            case nfsstat.NFSERR_ENCR_ALG_UNSUPP:
                return "NFS4ERR_ENCR_ALG_UNSUPP";
            case nfsstat.NFSERR_PNFS_NO_LAYOUT:
                return "NFS4ERR_PNFS_NO_LAYOUT";
            case nfsstat.NFSERR_NOT_ONLY_OP:
                return "NFS4ERR_NOT_ONLY_OP";
            case nfsstat.NFSERR_WRONG_CRED:
                return "NFS4ERR_WRONG_CRED";
            case nfsstat.NFSERR_WRONG_TYPE:
                return "NFS4ERR_WRONG_TYPE";
            case nfsstat.NFSERR_DIRDELEG_UNAVAIL:
                return "NFS4ERR_DIRDELEG_UNAVAIL";
            case nfsstat.NFSERR_REJECT_DELEG:
                return "NFS4ERR_REJECT_DELEG";
            case nfsstat.NFSERR_RETURNCONFLICT:
                return "NFS4ERR_RETURNCONFLICT";
            case nfsstat.NFS4ERR_DELEG_REVOKED:
                return "NFS4ERR_DELEG_REVOKED";
            case nfsstat.NFS4ERR_PARTNER_NOTSUPP:
                return "NFS4ERR_PARTNER_NOTSUPP";
            case nfsstat.NFS4ERR_PARTNER_NO_AUTH:
                return "NFS4ERR_PARTNER_NO_AUTH";
            case nfsstat.NFS4ERR_UNION_NOTSUPP:
                return "NFS4ERR_UNION_NOTSUPP";
            case nfsstat.NFS4ERR_OFFLOAD_DENIED:
                return "NFS4ERR_OFFLOAD_DENIED";
            case nfsstat.NFS4ERR_WRONG_LFS:
                return "NFS4ERR_WRONG_LFS";
            case nfsstat.NFS4ERR_BADLABEL:
                return "NFS4ERR_BADLABEL";
            case nfsstat.NFS4ERR_OFFLOAD_NO_REQS:
                return "NFS4ERR_OFFLOAD_NO_REQS";
            default:
                return "NFSERR_UNKNON(" + errorCode + ")";
        }
    }

    public static void throwIfNeeded(int errorCode) throws ChimeraNFSException {

        switch (errorCode) {
            case nfsstat.NFS_OK:
                return;
            case nfsstat.NFSERR_PERM:
                throw new PermException();
            case nfsstat.NFSERR_NOENT:
                throw new NoEntException();
            case nfsstat.NFSERR_IO:
                throw new NfsIoException();
            case nfsstat.NFSERR_NXIO:
                throw new NXioException();
            case nfsstat.NFSERR_ACCESS:
                throw new AccessException();
            case nfsstat.NFSERR_EXIST:
                throw new ExistException();
            case nfsstat.NFSERR_XDEV:
                throw new XDevException();
            case nfsstat.NFSERR_NODEV:
                throw new NoDevException();
            case nfsstat.NFSERR_NOTDIR:
                throw new NotDirException();
            case nfsstat.NFSERR_ISDIR:
                throw new IsDirException();
            case nfsstat.NFSERR_INVAL:
                throw new InvalException();
            case nfsstat.NFSERR_FBIG:
                throw new FBigException();
            case nfsstat.NFSERR_NOSPC:
                throw new NoSpcException();
            case nfsstat.NFSERR_ROFS:
                throw new RoFsException();
            case nfsstat.NFSERR_MLINK:
                throw new MLinkException();
            case nfsstat.NFSERR_NAMETOOLONG:
                throw new NameTooLongException();
            case nfsstat.NFSERR_NOTEMPTY:
                throw new NotEmptyException();
            case nfsstat.NFSERR_DQUOT:
                throw new DQuotException();
            case nfsstat.NFSERR_STALE:
                throw new StaleException();
            case nfsstat.NFSERR_REMOTE:
                throw new RemoteException();
            case nfsstat.NFSERR_WFLUSH:
                throw new WFlushException();
            case nfsstat.NFSERR_BADHANDLE:
                throw new BadHandleException();
            case nfsstat.NFSERR_NOT_SYNC:
                throw new NotSyncException();
            case nfsstat.NFSERR_BAD_COOKIE:
                throw new BadCookieException();
            case nfsstat.NFSERR_NOTSUPP:
                throw new NotSuppException();
            case nfsstat.NFSERR_TOOSMALL:
                throw new TooSmallException();
            case nfsstat.NFSERR_SERVERFAULT:
                throw new ServerFaultException();
            case nfsstat.NFSERR_BADTYPE:
                throw new BadTypeException();
            case nfsstat.NFSERR_DELAY:
                throw new DelayException();
            case nfsstat.NFSERR_SAME:
                throw new SameException();
            case nfsstat.NFSERR_DENIED:
                throw new DeniedException();
            case nfsstat.NFSERR_EXPIRED:
                throw new ExpiredException();
            case nfsstat.NFSERR_LOCKED:
                throw new LockedException();
            case nfsstat.NFSERR_GRACE:
                throw new GraceException();
            case nfsstat.NFSERR_FHEXPIRED:
                throw new FhExpiredException();
            case nfsstat.NFSERR_SHARE_DENIED:
                throw new ShareDeniedException();
            case nfsstat.NFSERR_WRONGSEC:
                throw new WrongSecException();
            case nfsstat.NFSERR_CLID_INUSE:
                throw new ClidInUseException();
            case nfsstat.NFSERR_RESOURCE:
                throw new ResourceException();
            case nfsstat.NFSERR_MOVED:
                throw new MovedException();
            case nfsstat.NFSERR_NOFILEHANDLE:
                throw new NoFileHandleException();
            case nfsstat.NFSERR_MINOR_VERS_MISMATCH:
                throw new MinorVersMismatchException();
            case nfsstat.NFSERR_STALE_CLIENTID:
                throw new StaleClientidException();
            case nfsstat.NFSERR_STALE_STATEID:
                throw new StaleStateidException();
            case nfsstat.NFSERR_OLD_STATEID:
                throw new OldStateidException();
            case nfsstat.NFSERR_BAD_STATEID:
                throw new BadStateidException();
            case nfsstat.NFSERR_BAD_SEQID:
                throw new BadSeqidException();
            case nfsstat.NFSERR_NOT_SAME:
                throw new NotSameException();
            case nfsstat.NFSERR_LOCK_RANGE:
                throw new LockRangeException();
            case nfsstat.NFSERR_SYMLINK:
                throw new SymlinkException();
            case nfsstat.NFSERR_RESTOREFH:
                throw new RestoreFhException();
            case nfsstat.NFSERR_LEASE_MOVED:
                throw new LeaseMovedException();
            case nfsstat.NFSERR_ATTRNOTSUPP:
                throw new AttrNotSuppException();
            case nfsstat.NFSERR_NO_GRACE:
                throw new NoGraceException();
            case nfsstat.NFSERR_RECLAIM_BAD:
                throw new ReclaimBadException();
            case nfsstat.NFSERR_RECLAIM_CONFLICT:
                throw new ReclaimConflictException();
            case nfsstat.NFSERR_BADXDR:
                throw new BadXdrException();
            case nfsstat.NFSERR_LOCKS_HELD:
                throw new LocksHeldException();
            case nfsstat.NFSERR_OPENMODE:
                throw new OpenModeException();
            case nfsstat.NFSERR_BADOWNER:
                throw new BadOwnerException();
            case nfsstat.NFSERR_BADCHAR:
                throw new BadCharException();
            case nfsstat.NFSERR_BADNAME:
                throw new BadNameException();
            case nfsstat.NFSERR_BAD_RANGE:
                throw new BadRangeException();
            case nfsstat.NFSERR_LOCK_NOTSUPP:
                throw new LockNotSuppException();
            case nfsstat.NFSERR_OP_ILLEGAL:
                throw new OpIllegalException();
            case nfsstat.NFSERR_DEADLOCK:
                throw new DeadLockedException();
            case nfsstat.NFSERR_FILE_OPEN:
                throw new FileOpenException();
            case nfsstat.NFSERR_ADMIN_REVOKED:
                throw new AdminRevokedException();
            case nfsstat.NFSERR_CB_PATH_DOWN:
                throw new CbPathDownException();
            case nfsstat.NFSERR_BADIOMODE:
                throw new BadIoModeException();
            case nfsstat.NFSERR_BADLAYOUT:
                throw new BadLayoutException();
            case nfsstat.NFSERR_BAD_SESSION_DIGEST:
                throw new BadSessionDigestException();
            case nfsstat.NFSERR_BADSESSION:
                throw new BadSessionException();
            case nfsstat.NFSERR_BADSLOT:
                throw new BadSlotException();
            case nfsstat.NFSERR_COMPLETE_ALREADY:
                throw new CompleteAlreadyException();
            case nfsstat.NFSERR_CONN_NOT_BOUND_TO_SESSION:
                throw new ConnNotBoundToSessionException();
            case nfsstat.NFSERR_DELEG_ALREADY_WANTED:
                throw new DelegAlreadyWantedException();
            case nfsstat.NFSERR_BACK_CHAN_BUSY:
                throw new BackChanBusyException();
            case nfsstat.NFSERR_LAYOUTTRYLATER:
                throw new LayoutTryLaterException();
            case nfsstat.NFSERR_LAYOUTUNAVAILABLE:
                throw new LayoutUnavailableException();
            case nfsstat.NFSERR_NOMATCHING_LAYOUT:
                throw new NoMatchingLayoutException();
            case nfsstat.NFSERR_RECALLCONFLICT:
                throw new RecallConflictException();
            case nfsstat.NFSERR_UNKNOWN_LAYOUTTYPE:
                throw new UnknownLayoutTypeException();
            case nfsstat.NFSERR_SEQ_MISORDERED:
                throw new SeqMisorderedException();
            case nfsstat.NFSERR_SEQUENCE_POS:
                throw new SequencePosException();
            case nfsstat.NFSERR_REQ_TOO_BIG:
                throw new ReqTooBigException();
            case nfsstat.NFSERR_REP_TOO_BIG:
                throw new RepTooBigException();
            case nfsstat.NFSERR_REP_TOO_BIG_TO_CACHE:
                throw new RepTooBigToCacheException();
            case nfsstat.NFSERR_RETRY_UNCACHED_REP:
                throw new RetryUncacheRepException();
            case nfsstat.NFSERR_UNSAFE_COMPOUND:
                throw new UnsafeCompoundException();
            case nfsstat.NFSERR_TOO_MANY_OPS:
                throw new TooManyOpsException();
            case nfsstat.NFSERR_OP_NOT_IN_SESSION:
                throw new OpNotInSessionException();
            case nfsstat.NFSERR_HASH_ALG_UNSUPP:
                throw new HashAlgUnsuppException();
            case nfsstat.NFSERR_CONN_BINDING_NOT_ENFORCED:
                throw new ConnBindingNotEnforcedException();
            case nfsstat.NFSERR_CLIENTID_BUSY:
                throw new ClientidBusyException();
            case nfsstat.NFSERR_PNFS_IO_HOLE:
                throw new PnfsIoHoleException();
            case nfsstat.NFSERR_SEQ_FALSE_RETRY:
                throw new SeqFalseRetryException();
            case nfsstat.NFSERR_BAD_HIGH_SLOT:
                throw new BadHighSlotException();
            case nfsstat.NFSERR_DEADSESSION:
                throw new DeadSessionException();
            case nfsstat.NFSERR_ENCR_ALG_UNSUPP:
                throw new EncrAlgUnsuppException();
            case nfsstat.NFSERR_PNFS_NO_LAYOUT:
                throw new PnfsNoLayoutException();
            case nfsstat.NFSERR_NOT_ONLY_OP:
                throw new NotOnlyOpException();
            case nfsstat.NFSERR_WRONG_CRED:
                throw new WrongCredException();
            case nfsstat.NFSERR_WRONG_TYPE:
                throw new WrongTypeException();
            case nfsstat.NFSERR_DIRDELEG_UNAVAIL:
                throw new DirDelegUnavailException();
            case nfsstat.NFSERR_REJECT_DELEG:
                throw new RejectDelegException();
            case nfsstat.NFSERR_RETURNCONFLICT:
                throw new ReturnConflictException();
            default:
                throw new BadXdrException();
        }
    }
}
// End of nfsstat4.java
