/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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

import java.io.IOException;

import org.dcache.nfs.nfsstat;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class nfs_resop4 implements XdrAble {
    public int resop;
    public ACCESS4res opaccess;
    public CLOSE4res opclose;
    public COMMIT4res opcommit;
    public CREATE4res opcreate;
    public DELEGPURGE4res opdelegpurge;
    public DELEGRETURN4res opdelegreturn;
    public GETATTR4res opgetattr;
    public GETFH4res opgetfh;
    public LINK4res oplink;
    public LOCK4res oplock;
    public LOCKT4res oplockt;
    public LOCKU4res oplocku;
    public LOOKUP4res oplookup;
    public LOOKUPP4res oplookupp;
    public NVERIFY4res opnverify;
    public OPEN4res opopen;
    public OPENATTR4res opopenattr;
    public OPEN_CONFIRM4res opopen_confirm;
    public OPEN_DOWNGRADE4res opopen_downgrade;
    public PUTFH4res opputfh;
    public PUTPUBFH4res opputpubfh;
    public PUTROOTFH4res opputrootfh;
    public READ4res opread;
    public READDIR4res opreaddir;
    public READLINK4res opreadlink;
    public REMOVE4res opremove;
    public RENAME4res oprename;
    public RENEW4res oprenew;
    public RESTOREFH4res oprestorefh;
    public SAVEFH4res opsavefh;
    public SECINFO4res opsecinfo;
    public SETATTR4res opsetattr;
    public SETCLIENTID4res opsetclientid;
    public SETCLIENTID_CONFIRM4res opsetclientid_confirm;
    public VERIFY4res opverify;
    public WRITE4res opwrite;
    public RELEASE_LOCKOWNER4res oprelease_lockowner;
    public BACKCHANNEL_CTL4res opbackchannel_ctl;
    public BIND_CONN_TO_SESSION4res opbind_conn_to_session;
    public EXCHANGE_ID4res opexchange_id;
    public CREATE_SESSION4res opcreate_session;
    public DESTROY_SESSION4res opdestroy_session;
    public FREE_STATEID4res opfree_stateid;
    public GET_DIR_DELEGATION4res opget_dir_delegation;
    public GETDEVICEINFO4res opgetdeviceinfo;
    public GETDEVICELIST4res opgetdevicelist;
    public LAYOUTCOMMIT4res oplayoutcommit;
    public LAYOUTERROR4res oplayouterror;
    public LAYOUTGET4res oplayoutget;
    public LAYOUTRETURN4res oplayoutreturn;
    public LAYOUTSTATS4res oplayoutstats;
    public SECINFO_NO_NAME4res opsecinfo_no_name;
    public SEQUENCE4res opsequence;
    public SET_SSV4res opset_ssv;
    public TEST_STATEID4res optest_stateid;
    public WANT_DELEGATION4res opwant_delegation;
    public DESTROY_CLIENTID4res opdestroy_clientid;
    public RECLAIM_COMPLETE4res opreclaim_complete;
    public ALLOCATE4res opallocate;
    public COPY4res opcopy;
    public COPY_NOTIFY4res opcopy_notify;
    public DEALLOCATE4res opdeallocate;
    public IO_ADVISE4res opio_advise;
    public OFFLOAD_CANCEL4res opoffload_cancel;
    public OFFLOAD_STATUS4res opoffload_status;
    public READ_PLUS4res opread_plus;
    public SEEK4res opseek;
    public WRITE_SAME4res opwrite_same;
    public CLONE4res opclone;
    public GETXATTR4res opgetxattr;
    public SETXATTR4res opsetxattr;
    public LISTXATTRS4res oplistxattrs;
    public REMOVEXATTR4res opremovexattr;

    public ILLEGAL4res opillegal;

    public nfs_resop4() {
    }

    public nfs_resop4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(resop);
        switch (resop) {
            case nfs_opnum4.OP_ACCESS:
                opaccess.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_CLOSE:
                opclose.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_COMMIT:
                opcommit.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_CREATE:
                opcreate.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_DELEGPURGE:
                opdelegpurge.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_DELEGRETURN:
                opdelegreturn.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_GETATTR:
                opgetattr.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_GETFH:
                opgetfh.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LINK:
                oplink.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LOCK:
                oplock.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LOCKT:
                oplockt.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LOCKU:
                oplocku.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LOOKUP:
                oplookup.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LOOKUPP:
                oplookupp.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_NVERIFY:
                opnverify.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_OPEN:
                opopen.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_OPENATTR:
                opopenattr.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_OPEN_CONFIRM:
                opopen_confirm.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                opopen_downgrade.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_PUTFH:
                opputfh.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_PUTPUBFH:
                opputpubfh.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                opputrootfh.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_READ:
                opread.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_READDIR:
                opreaddir.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_READLINK:
                opreadlink.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_REMOVE:
                opremove.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_RENAME:
                oprename.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_RENEW:
                oprenew.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_RESTOREFH:
                oprestorefh.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SAVEFH:
                opsavefh.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SECINFO:
                opsecinfo.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SETATTR:
                opsetattr.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SETCLIENTID:
                opsetclientid.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                opsetclientid_confirm.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_VERIFY:
                opverify.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_WRITE:
                opwrite.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                oprelease_lockowner.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                opbackchannel_ctl.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                opbind_conn_to_session.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_EXCHANGE_ID:
                opexchange_id.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_CREATE_SESSION:
                opcreate_session.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_DESTROY_SESSION:
                opdestroy_session.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_FREE_STATEID:
                opfree_stateid.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                opget_dir_delegation.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_GETDEVICEINFO:
                opgetdeviceinfo.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_GETDEVICELIST:
                opgetdevicelist.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                oplayoutcommit.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTERROR:
                oplayouterror.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTGET:
                oplayoutget.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTRETURN:
                oplayoutreturn.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTSTATS:
                oplayoutstats.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                opsecinfo_no_name.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SEQUENCE:
                opsequence.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SET_SSV:
                opset_ssv.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_TEST_STATEID:
                optest_stateid.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_WANT_DELEGATION:
                opwant_delegation.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                opdestroy_clientid.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                opreclaim_complete.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_ALLOCATE:
                opallocate.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_COPY:
                opcopy.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_COPY_NOTIFY:
                opcopy_notify.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_DEALLOCATE:
                opdeallocate.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_IO_ADVISE:
                opio_advise.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                opoffload_cancel.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                opoffload_status.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_READ_PLUS:
                opread_plus.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SEEK:
                opseek.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_WRITE_SAME:
                opwrite_same.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_CLONE:
                opclone.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_GETXATTR:
                opgetxattr.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_SETXATTR:
                opsetxattr.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_LISTXATTRS:
                oplistxattrs.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_REMOVEXATTR:
                opremovexattr.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_ILLEGAL:
                opillegal.xdrEncode(xdr);
                break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        resop = xdr.xdrDecodeInt();
        switch (resop) {
            case nfs_opnum4.OP_ACCESS:
                opaccess = new ACCESS4res(xdr);
                break;
            case nfs_opnum4.OP_CLOSE:
                opclose = new CLOSE4res(xdr);
                break;
            case nfs_opnum4.OP_COMMIT:
                opcommit = new COMMIT4res(xdr);
                break;
            case nfs_opnum4.OP_CREATE:
                opcreate = new CREATE4res(xdr);
                break;
            case nfs_opnum4.OP_DELEGPURGE:
                opdelegpurge = new DELEGPURGE4res(xdr);
                break;
            case nfs_opnum4.OP_DELEGRETURN:
                opdelegreturn = new DELEGRETURN4res(xdr);
                break;
            case nfs_opnum4.OP_GETATTR:
                opgetattr = new GETATTR4res(xdr);
                break;
            case nfs_opnum4.OP_GETFH:
                opgetfh = new GETFH4res(xdr);
                break;
            case nfs_opnum4.OP_LINK:
                oplink = new LINK4res(xdr);
                break;
            case nfs_opnum4.OP_LOCK:
                oplock = new LOCK4res(xdr);
                break;
            case nfs_opnum4.OP_LOCKT:
                oplockt = new LOCKT4res(xdr);
                break;
            case nfs_opnum4.OP_LOCKU:
                oplocku = new LOCKU4res(xdr);
                break;
            case nfs_opnum4.OP_LOOKUP:
                oplookup = new LOOKUP4res(xdr);
                break;
            case nfs_opnum4.OP_LOOKUPP:
                oplookupp = new LOOKUPP4res(xdr);
                break;
            case nfs_opnum4.OP_NVERIFY:
                opnverify = new NVERIFY4res(xdr);
                break;
            case nfs_opnum4.OP_OPEN:
                opopen = new OPEN4res(xdr);
                break;
            case nfs_opnum4.OP_OPENATTR:
                opopenattr = new OPENATTR4res(xdr);
                break;
            case nfs_opnum4.OP_OPEN_CONFIRM:
                opopen_confirm = new OPEN_CONFIRM4res(xdr);
                break;
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                opopen_downgrade = new OPEN_DOWNGRADE4res(xdr);
                break;
            case nfs_opnum4.OP_PUTFH:
                opputfh = new PUTFH4res(xdr);
                break;
            case nfs_opnum4.OP_PUTPUBFH:
                opputpubfh = new PUTPUBFH4res(xdr);
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                opputrootfh = new PUTROOTFH4res(xdr);
                break;
            case nfs_opnum4.OP_READ:
                opread = new READ4res(xdr);
                break;
            case nfs_opnum4.OP_READDIR:
                opreaddir = new READDIR4res(xdr);
                break;
            case nfs_opnum4.OP_READLINK:
                opreadlink = new READLINK4res(xdr);
                break;
            case nfs_opnum4.OP_REMOVE:
                opremove = new REMOVE4res(xdr);
                break;
            case nfs_opnum4.OP_RENAME:
                oprename = new RENAME4res(xdr);
                break;
            case nfs_opnum4.OP_RENEW:
                oprenew = new RENEW4res(xdr);
                break;
            case nfs_opnum4.OP_RESTOREFH:
                oprestorefh = new RESTOREFH4res(xdr);
                break;
            case nfs_opnum4.OP_SAVEFH:
                opsavefh = new SAVEFH4res(xdr);
                break;
            case nfs_opnum4.OP_SECINFO:
                opsecinfo = new SECINFO4res(xdr);
                break;
            case nfs_opnum4.OP_SETATTR:
                opsetattr = new SETATTR4res(xdr);
                break;
            case nfs_opnum4.OP_SETCLIENTID:
                opsetclientid = new SETCLIENTID4res(xdr);
                break;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                opsetclientid_confirm = new SETCLIENTID_CONFIRM4res(xdr);
                break;
            case nfs_opnum4.OP_VERIFY:
                opverify = new VERIFY4res(xdr);
                break;
            case nfs_opnum4.OP_WRITE:
                opwrite = new WRITE4res(xdr);
                break;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                oprelease_lockowner = new RELEASE_LOCKOWNER4res(xdr);
                break;
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                opbackchannel_ctl = new BACKCHANNEL_CTL4res(xdr);
                break;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                opbind_conn_to_session = new BIND_CONN_TO_SESSION4res(xdr);
                break;
            case nfs_opnum4.OP_EXCHANGE_ID:
                opexchange_id = new EXCHANGE_ID4res(xdr);
                break;
            case nfs_opnum4.OP_CREATE_SESSION:
                opcreate_session = new CREATE_SESSION4res(xdr);
                break;
            case nfs_opnum4.OP_DESTROY_SESSION:
                opdestroy_session = new DESTROY_SESSION4res(xdr);
                break;
            case nfs_opnum4.OP_FREE_STATEID:
                opfree_stateid = new FREE_STATEID4res(xdr);
                break;
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                opget_dir_delegation = new GET_DIR_DELEGATION4res(xdr);
                break;
            case nfs_opnum4.OP_GETDEVICEINFO:
                opgetdeviceinfo = new GETDEVICEINFO4res(xdr);
                break;
            case nfs_opnum4.OP_GETDEVICELIST:
                opgetdevicelist = new GETDEVICELIST4res(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                oplayoutcommit = new LAYOUTCOMMIT4res(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTERROR:
                oplayouterror = new LAYOUTERROR4res();
                break;
            case nfs_opnum4.OP_LAYOUTGET:
                oplayoutget = new LAYOUTGET4res(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTSTATS:
                oplayoutstats = new LAYOUTSTATS4res(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTRETURN:
                oplayoutreturn = new LAYOUTRETURN4res(xdr);
                break;
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                opsecinfo_no_name = new SECINFO_NO_NAME4res(xdr);
                break;
            case nfs_opnum4.OP_SEQUENCE:
                opsequence = new SEQUENCE4res(xdr);
                break;
            case nfs_opnum4.OP_SET_SSV:
                opset_ssv = new SET_SSV4res(xdr);
                break;
            case nfs_opnum4.OP_TEST_STATEID:
                optest_stateid = new TEST_STATEID4res(xdr);
                break;
            case nfs_opnum4.OP_WANT_DELEGATION:
                opwant_delegation = new WANT_DELEGATION4res(xdr);
                break;
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                opdestroy_clientid = new DESTROY_CLIENTID4res(xdr);
                break;
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                opreclaim_complete = new RECLAIM_COMPLETE4res(xdr);
                break;
            case nfs_opnum4.OP_GETXATTR:
                opgetxattr = new GETXATTR4res(xdr);
                break;
            case nfs_opnum4.OP_SETXATTR:
                opsetxattr = new SETXATTR4res(xdr);
                break;
            case nfs_opnum4.OP_LISTXATTRS:
                oplistxattrs = new LISTXATTRS4res(xdr);
                break;
            case nfs_opnum4.OP_REMOVEXATTR:
                opremovexattr = new REMOVEXATTR4res(xdr);
                break;
            case nfs_opnum4.OP_ALLOCATE:
                opallocate = new ALLOCATE4res(xdr);
                break;
            case nfs_opnum4.OP_COPY:
                opcopy = new COPY4res(xdr);
                break;
            case nfs_opnum4.OP_COPY_NOTIFY:
                opcopy_notify = new COPY_NOTIFY4res(xdr);
                break;
            case nfs_opnum4.OP_DEALLOCATE:
                opdeallocate = new DEALLOCATE4res(xdr);
                break;
            case nfs_opnum4.OP_IO_ADVISE:
                opio_advise = new IO_ADVISE4res(xdr);
                break;
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                opoffload_cancel = new OFFLOAD_CANCEL4res(xdr);
                break;
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                opoffload_status = new OFFLOAD_STATUS4res(xdr);
                break;
            case nfs_opnum4.OP_READ_PLUS:
                opread_plus = new READ_PLUS4res(xdr);
                break;
            case nfs_opnum4.OP_SEEK:
                opseek = new SEEK4res(xdr);
                break;
            case nfs_opnum4.OP_WRITE_SAME:
                opwrite_same = new WRITE_SAME4res(xdr);
                break;
            case nfs_opnum4.OP_CLONE:
                opclone = new CLONE4res(xdr);
                break;
            case nfs_opnum4.OP_ILLEGAL:
                opillegal = new ILLEGAL4res(xdr);
                break;
        }
    }

    public int getStatus() {

        int status = nfsstat.NFSERR_SERVERFAULT;

        switch (resop) {
            case nfs_opnum4.OP_ACCESS:
                status = opaccess.status;
                break;
            case nfs_opnum4.OP_CLOSE:
                status = opclose.status;
                break;
            case nfs_opnum4.OP_COMMIT:
                status = opcommit.status;
                break;
            case nfs_opnum4.OP_CREATE:
                status = opcreate.status;
                break;
            case nfs_opnum4.OP_DELEGPURGE:
                status = opdelegpurge.status;
                break;
            case nfs_opnum4.OP_DELEGRETURN:
                status = opdelegreturn.status;
                break;
            case nfs_opnum4.OP_GETATTR:
                status = opgetattr.status;
                break;
            case nfs_opnum4.OP_GETFH:
                status = opgetfh.status;
                break;
            case nfs_opnum4.OP_LINK:
                status = oplink.status;
                break;
            case nfs_opnum4.OP_LOCK:
                status = oplock.status;
                break;
            case nfs_opnum4.OP_LOCKT:
                status = oplockt.status;
                break;
            case nfs_opnum4.OP_LOCKU:
                status = oplocku.status;
                break;
            case nfs_opnum4.OP_LOOKUP:
                status = oplookup.status;
                break;
            case nfs_opnum4.OP_LOOKUPP:
                status = oplookupp.status;
                break;
            case nfs_opnum4.OP_NVERIFY:
                status = opnverify.status;
                break;
            case nfs_opnum4.OP_OPEN:
                status = opopen.status;
                break;
            case nfs_opnum4.OP_OPENATTR:
                status = opopenattr.status;
                break;
            case nfs_opnum4.OP_OPEN_CONFIRM:
                status = opopen_confirm.status;
                break;
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                status = opopen_downgrade.status;
                break;
            case nfs_opnum4.OP_PUTFH:
                status = opputfh.status;
                break;
            case nfs_opnum4.OP_PUTPUBFH:
                status = opputpubfh.status;
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                status = opputrootfh.status;
                break;
            case nfs_opnum4.OP_READ:
                status = opread.status;
                break;
            case nfs_opnum4.OP_READDIR:
                status = opreaddir.status;
                break;
            case nfs_opnum4.OP_READLINK:
                status = opreadlink.status;
                break;
            case nfs_opnum4.OP_REMOVE:
                status = opremove.status;
                break;
            case nfs_opnum4.OP_RENAME:
                status = oprename.status;
                break;
            case nfs_opnum4.OP_RENEW:
                status = oprenew.status;
                break;
            case nfs_opnum4.OP_RESTOREFH:
                status = oprestorefh.status;
                break;
            case nfs_opnum4.OP_SAVEFH:
                status = opsavefh.status;
                break;
            case nfs_opnum4.OP_SECINFO:
                status = opsecinfo.status;
                break;
            case nfs_opnum4.OP_SETATTR:
                status = opsetattr.status;
                break;
            case nfs_opnum4.OP_SETCLIENTID:
                status = opsetclientid.status;
                break;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                status = opsetclientid_confirm.status;
                break;
            case nfs_opnum4.OP_VERIFY:
                status = opverify.status;
                break;
            case nfs_opnum4.OP_WRITE:
                status = opwrite.status;
                break;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                status = oprelease_lockowner.status;
                break;
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                status = opbackchannel_ctl.bcr_status;
                break;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                status = opbind_conn_to_session.bctsr_status;
                break;
            case nfs_opnum4.OP_EXCHANGE_ID:
                status = opexchange_id.eir_status;
                break;
            case nfs_opnum4.OP_CREATE_SESSION:
                status = opcreate_session.csr_status;
                break;
            case nfs_opnum4.OP_DESTROY_SESSION:
                status = opdestroy_session.dsr_status;
                break;
            case nfs_opnum4.OP_FREE_STATEID:
                status = opfree_stateid.fsr_status;
                break;
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                status = opget_dir_delegation.gddr_status;
                break;
            case nfs_opnum4.OP_GETDEVICEINFO:
                status = opgetdeviceinfo.gdir_status;
                break;
            case nfs_opnum4.OP_GETDEVICELIST:
                status = opgetdevicelist.gdlr_status;
                break;
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                status = oplayoutcommit.locr_status;
                break;
            case nfs_opnum4.OP_LAYOUTERROR:
                status = oplayouterror.status;
                break;
            case nfs_opnum4.OP_LAYOUTGET:
                status = oplayoutget.logr_status;
                break;
            case nfs_opnum4.OP_LAYOUTRETURN:
                status = oplayoutreturn.lorr_status;
                break;
            case nfs_opnum4.OP_LAYOUTSTATS:
                status = oplayoutstats.status;
                break;
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                status = opsecinfo_no_name.status;
                break;
            case nfs_opnum4.OP_SEQUENCE:
                status = opsequence.sr_status;
                break;
            case nfs_opnum4.OP_SET_SSV:
                status = opset_ssv.ssr_status;
                break;
            case nfs_opnum4.OP_TEST_STATEID:
                status = optest_stateid.tsr_status;
                break;
            case nfs_opnum4.OP_WANT_DELEGATION:
                status = opwant_delegation.wdr_status;
                break;
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                status = opdestroy_clientid.dcr_status;
                break;
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                status = opreclaim_complete.rcr_status;
                break;
            case nfs_opnum4.OP_ALLOCATE:
                status = opallocate.ar_status;
                break;
            case nfs_opnum4.OP_CLONE:
                status = opclone.cl_status;
                break;
            case nfs_opnum4.OP_COPY:
                status = opcopy.cr_status;
                break;
            case nfs_opnum4.OP_COPY_NOTIFY:
                status = opcopy_notify.cnr_status;
                break;
            case nfs_opnum4.OP_DEALLOCATE:
                status = opdeallocate.dr_status;
                break;
            case nfs_opnum4.OP_IO_ADVISE:
                status = opio_advise.ior_status;
                break;
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                status = opoffload_cancel.ocr_status;
                break;
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                status = opoffload_status.osr_status;
                break;
            case nfs_opnum4.OP_READ_PLUS:
                status = opread_plus.rp_status;
                break;
            case nfs_opnum4.OP_SEEK:
                status = opseek.sa_status;
                break;
            case nfs_opnum4.OP_WRITE_SAME:
                status = opwrite_same.wsr_status;
                break;
            case nfs_opnum4.OP_GETXATTR:
                status = opgetxattr.gxr_status;
                break;
            case nfs_opnum4.OP_SETXATTR:
                status = opsetxattr.sxr_status;
                break;
            case nfs_opnum4.OP_LISTXATTRS:
                status = oplistxattrs.lxr_status;
                break;
            case nfs_opnum4.OP_REMOVEXATTR:
                status = opremovexattr.rxr_status;
                break;
            case nfs_opnum4.OP_ILLEGAL:
                status = opillegal.status;
                break;
        }
        return status;
    }

    public void setStatus(int status) {

        switch (resop) {
            case nfs_opnum4.OP_ACCESS:
                opaccess.status = status;
                break;
            case nfs_opnum4.OP_CLOSE:
                opclose.status = status;
                break;
            case nfs_opnum4.OP_COMMIT:
                opcommit.status = status;
                break;
            case nfs_opnum4.OP_CREATE:
                opcreate.status = status;
                break;
            case nfs_opnum4.OP_DELEGPURGE:
                opdelegpurge.status = status;
                break;
            case nfs_opnum4.OP_DELEGRETURN:
                opdelegreturn.status = status;
                break;
            case nfs_opnum4.OP_GETATTR:
                opgetattr.status = status;
                break;
            case nfs_opnum4.OP_GETFH:
                opgetfh.status = status;
                break;
            case nfs_opnum4.OP_LINK:
                oplink.status = status;
                break;
            case nfs_opnum4.OP_LOCK:
                oplock.status = status;
                break;
            case nfs_opnum4.OP_LOCKT:
                oplockt.status = status;
                break;
            case nfs_opnum4.OP_LOCKU:
                oplocku.status = status;
                break;
            case nfs_opnum4.OP_LOOKUP:
                oplookup.status = status;
                break;
            case nfs_opnum4.OP_LOOKUPP:
                oplookupp.status = status;
                break;
            case nfs_opnum4.OP_NVERIFY:
                opnverify.status = status;
                break;
            case nfs_opnum4.OP_OPEN:
                opopen.status = status;
                break;
            case nfs_opnum4.OP_OPENATTR:
                opopenattr.status = status;
                break;
            case nfs_opnum4.OP_OPEN_CONFIRM:
                opopen_confirm.status = status;
                break;
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                opopen_downgrade.status = status;
                break;
            case nfs_opnum4.OP_PUTFH:
                opputfh.status = status;
                break;
            case nfs_opnum4.OP_PUTPUBFH:
                opputpubfh.status = status;
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                opputrootfh.status = status;
                break;
            case nfs_opnum4.OP_READ:
                opread.status = status;
                break;
            case nfs_opnum4.OP_READDIR:
                opreaddir.status = status;
                break;
            case nfs_opnum4.OP_READLINK:
                opreadlink.status = status;
                break;
            case nfs_opnum4.OP_REMOVE:
                opremove.status = status;
                break;
            case nfs_opnum4.OP_RENAME:
                oprename.status = status;
                break;
            case nfs_opnum4.OP_RENEW:
                oprenew.status = status;
                break;
            case nfs_opnum4.OP_RESTOREFH:
                oprestorefh.status = status;
                break;
            case nfs_opnum4.OP_SAVEFH:
                opsavefh.status = status;
                break;
            case nfs_opnum4.OP_SECINFO:
                opsecinfo.status = status;
                break;
            case nfs_opnum4.OP_SETATTR:
                opsetattr.status = status;
                break;
            case nfs_opnum4.OP_SETCLIENTID:
                opsetclientid.status = status;
                break;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                opsetclientid_confirm.status = status;
                break;
            case nfs_opnum4.OP_VERIFY:
                opverify.status = status;
                break;
            case nfs_opnum4.OP_WRITE:
                opwrite.status = status;
                break;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                oprelease_lockowner.status = status;
                break;
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                opbackchannel_ctl.bcr_status = status;
                break;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                opbind_conn_to_session.bctsr_status = status;
                break;
            case nfs_opnum4.OP_EXCHANGE_ID:
                opexchange_id.eir_status = status;
                break;
            case nfs_opnum4.OP_CREATE_SESSION:
                opcreate_session.csr_status = status;
                break;
            case nfs_opnum4.OP_DESTROY_SESSION:
                opdestroy_session.dsr_status = status;
                break;
            case nfs_opnum4.OP_FREE_STATEID:
                opfree_stateid.fsr_status = status;
                break;
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                opget_dir_delegation.gddr_status = status;
                break;
            case nfs_opnum4.OP_GETDEVICEINFO:
                opgetdeviceinfo.gdir_status = status;
                break;
            case nfs_opnum4.OP_GETDEVICELIST:
                opgetdevicelist.gdlr_status = status;
                break;
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                oplayoutcommit.locr_status = status;
                break;
            case nfs_opnum4.OP_LAYOUTERROR:
                oplayouterror.status = status;
                break;
            case nfs_opnum4.OP_LAYOUTGET:
                oplayoutget.logr_status = status;
                break;
            case nfs_opnum4.OP_LAYOUTRETURN:
                oplayoutreturn.lorr_status = status;
                break;
            case nfs_opnum4.OP_LAYOUTSTATS:
                oplayoutstats.status = status;
                break;
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                opsecinfo_no_name.status = status;
                break;
            case nfs_opnum4.OP_SEQUENCE:
                opsequence.sr_status = status;
                break;
            case nfs_opnum4.OP_SET_SSV:
                opset_ssv.ssr_status = status;
                break;
            case nfs_opnum4.OP_TEST_STATEID:
                optest_stateid.tsr_status = status;
                break;
            case nfs_opnum4.OP_WANT_DELEGATION:
                opwant_delegation.wdr_status = status;
                break;
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                opdestroy_clientid.dcr_status = status;
                break;
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                opreclaim_complete.rcr_status = status;
                break;
            case nfs_opnum4.OP_ALLOCATE:
                opallocate.ar_status = status;
                break;
            case nfs_opnum4.OP_CLONE:
                opclone.cl_status = status;
                break;
            case nfs_opnum4.OP_COPY:
                opcopy.cr_status = status;
                break;
            case nfs_opnum4.OP_COPY_NOTIFY:
                opcopy_notify.cnr_status = status;
                break;
            case nfs_opnum4.OP_DEALLOCATE:
                opdeallocate.dr_status = status;
                break;
            case nfs_opnum4.OP_IO_ADVISE:
                opio_advise.ior_status = status;
                break;
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                opoffload_cancel.ocr_status = status;
                break;
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                opoffload_status.osr_status = status;
                break;
            case nfs_opnum4.OP_READ_PLUS:
                opread_plus.rp_status = status;
                break;
            case nfs_opnum4.OP_SEEK:
                opseek.sa_status = status;
                break;
            case nfs_opnum4.OP_WRITE_SAME:
                opwrite_same.wsr_status = status;
                break;
            case nfs_opnum4.OP_GETXATTR:
                opgetxattr.gxr_status = status;
                break;
            case nfs_opnum4.OP_SETXATTR:
                opsetxattr.sxr_status = status;
                break;
            case nfs_opnum4.OP_LISTXATTRS:
                oplistxattrs.lxr_status = status;
                break;
            case nfs_opnum4.OP_REMOVEXATTR:
                opremovexattr.rxr_status = status;
                break;
            case nfs_opnum4.OP_ILLEGAL:
                opillegal.status = status;
                break;
        }
    }

    public static nfs_resop4 resopFor(int opcode) {
        nfs_resop4 resop = new nfs_resop4();
        resop.resop = opcode;

        switch (resop.resop) {
            case nfs_opnum4.OP_ACCESS:
                resop.opaccess = new ACCESS4res();
                break;
            case nfs_opnum4.OP_CLOSE:
                resop.opclose = new CLOSE4res();
                break;
            case nfs_opnum4.OP_COMMIT:
                resop.opcommit = new COMMIT4res();
                break;
            case nfs_opnum4.OP_CREATE:
                resop.opcreate = new CREATE4res();
                break;
            case nfs_opnum4.OP_DELEGPURGE:
                resop.opdelegpurge = new DELEGPURGE4res();
                break;
            case nfs_opnum4.OP_DELEGRETURN:
                resop.opdelegreturn = new DELEGRETURN4res();
                break;
            case nfs_opnum4.OP_GETATTR:
                resop.opgetattr = new GETATTR4res();
                break;
            case nfs_opnum4.OP_GETFH:
                resop.opgetfh = new GETFH4res();
                break;
            case nfs_opnum4.OP_LINK:
                resop.oplink = new LINK4res();
                break;
            case nfs_opnum4.OP_LOCK:
                resop.oplock = new LOCK4res();
                break;
            case nfs_opnum4.OP_LOCKT:
                resop.oplockt = new LOCKT4res();
                break;
            case nfs_opnum4.OP_LOCKU:
                resop.oplocku = new LOCKU4res();
                break;
            case nfs_opnum4.OP_LOOKUP:
                resop.oplookup = new LOOKUP4res();
                break;
            case nfs_opnum4.OP_LOOKUPP:
                resop.oplookupp = new LOOKUPP4res();
                break;
            case nfs_opnum4.OP_NVERIFY:
                resop.opnverify = new NVERIFY4res();
                break;
            case nfs_opnum4.OP_OPEN:
                resop.opopen = new OPEN4res();
                break;
            case nfs_opnum4.OP_OPENATTR:
                resop.opopenattr = new OPENATTR4res();
                break;
            case nfs_opnum4.OP_OPEN_CONFIRM:
                resop.opopen_confirm = new OPEN_CONFIRM4res();
                break;
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                resop.opopen_downgrade = new OPEN_DOWNGRADE4res();
                break;
            case nfs_opnum4.OP_PUTFH:
                resop.opputfh = new PUTFH4res();
                break;
            case nfs_opnum4.OP_PUTPUBFH:
                resop.opputpubfh = new PUTPUBFH4res();
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                resop.opputrootfh = new PUTROOTFH4res();
                break;
            case nfs_opnum4.OP_READ:
                resop.opread = new READ4res();
                break;
            case nfs_opnum4.OP_READDIR:
                resop.opreaddir = new READDIR4res();
                break;
            case nfs_opnum4.OP_READLINK:
                resop.opreadlink = new READLINK4res();
                break;
            case nfs_opnum4.OP_REMOVE:
                resop.opremove = new REMOVE4res();
                break;
            case nfs_opnum4.OP_RENAME:
                resop.oprename = new RENAME4res();
                break;
            case nfs_opnum4.OP_RENEW:
                resop.oprenew = new RENEW4res();
                break;
            case nfs_opnum4.OP_RESTOREFH:
                resop.oprestorefh = new RESTOREFH4res();
                break;
            case nfs_opnum4.OP_SAVEFH:
                resop.opsavefh = new SAVEFH4res();
                break;
            case nfs_opnum4.OP_SECINFO:
                resop.opsecinfo = new SECINFO4res();
                break;
            case nfs_opnum4.OP_SETATTR:
                resop.opsetattr = new SETATTR4res();
                break;
            case nfs_opnum4.OP_SETCLIENTID:
                resop.opsetclientid = new SETCLIENTID4res();
                break;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                resop.opsetclientid_confirm = new SETCLIENTID_CONFIRM4res();
                break;
            case nfs_opnum4.OP_VERIFY:
                resop.opverify = new VERIFY4res();
                break;
            case nfs_opnum4.OP_WRITE:
                resop.opwrite = new WRITE4res();
                break;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                resop.oprelease_lockowner = new RELEASE_LOCKOWNER4res();
                break;
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                resop.opbackchannel_ctl = new BACKCHANNEL_CTL4res();
                break;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                resop.opbind_conn_to_session = new BIND_CONN_TO_SESSION4res();
                break;
            case nfs_opnum4.OP_EXCHANGE_ID:
                resop.opexchange_id = new EXCHANGE_ID4res();
                break;
            case nfs_opnum4.OP_CREATE_SESSION:
                resop.opcreate_session = new CREATE_SESSION4res();
                break;
            case nfs_opnum4.OP_DESTROY_SESSION:
                resop.opdestroy_session = new DESTROY_SESSION4res();
                break;
            case nfs_opnum4.OP_FREE_STATEID:
                resop.opfree_stateid = new FREE_STATEID4res();
                break;
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                resop.opget_dir_delegation = new GET_DIR_DELEGATION4res();
                break;
            case nfs_opnum4.OP_GETDEVICEINFO:
                resop.opgetdeviceinfo = new GETDEVICEINFO4res();
                break;
            case nfs_opnum4.OP_GETDEVICELIST:
                resop.opgetdevicelist = new GETDEVICELIST4res();
                break;
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                resop.oplayoutcommit = new LAYOUTCOMMIT4res();
                break;
            case nfs_opnum4.OP_LAYOUTERROR:
                resop.oplayouterror = new LAYOUTERROR4res();
                break;
            case nfs_opnum4.OP_LAYOUTGET:
                resop.oplayoutget = new LAYOUTGET4res();
                break;
            case nfs_opnum4.OP_LAYOUTRETURN:
                resop.oplayoutreturn = new LAYOUTRETURN4res();
                break;
            case nfs_opnum4.OP_LAYOUTSTATS:
                resop.oplayoutstats = new LAYOUTSTATS4res();
                break;
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                resop.opsecinfo_no_name = new SECINFO_NO_NAME4res();
                break;
            case nfs_opnum4.OP_SEQUENCE:
                resop.opsequence = new SEQUENCE4res();
                break;
            case nfs_opnum4.OP_SET_SSV:
                resop.opset_ssv = new SET_SSV4res();
                break;
            case nfs_opnum4.OP_TEST_STATEID:
                resop.optest_stateid = new TEST_STATEID4res();
                break;
            case nfs_opnum4.OP_WANT_DELEGATION:
                resop.opwant_delegation = new WANT_DELEGATION4res();
                break;
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                resop.opdestroy_clientid = new DESTROY_CLIENTID4res();
                break;
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                resop.opreclaim_complete = new RECLAIM_COMPLETE4res();
                break;
            case nfs_opnum4.OP_ALLOCATE:
                resop.opallocate = new ALLOCATE4res();
                break;
            case nfs_opnum4.OP_CLONE:
                resop.opclone = new CLONE4res();
                break;
            case nfs_opnum4.OP_COPY:
                resop.opcopy = new COPY4res();
                break;
            case nfs_opnum4.OP_COPY_NOTIFY:
                resop.opcopy_notify = new COPY_NOTIFY4res();
                break;
            case nfs_opnum4.OP_DEALLOCATE:
                resop.opdeallocate = new DEALLOCATE4res();
                break;
            case nfs_opnum4.OP_IO_ADVISE:
                resop.opio_advise = new IO_ADVISE4res();
                break;
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                resop.opoffload_cancel = new OFFLOAD_CANCEL4res();
                break;
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                resop.opoffload_status = new OFFLOAD_STATUS4res();
                break;
            case nfs_opnum4.OP_READ_PLUS:
                resop.opread_plus = new READ_PLUS4res();
                break;
            case nfs_opnum4.OP_SEEK:
                resop.opseek = new SEEK4res();
                break;
            case nfs_opnum4.OP_WRITE_SAME:
                resop.opwrite_same = new WRITE_SAME4res();
                break;
            case nfs_opnum4.OP_GETXATTR:
                resop.opgetxattr = new GETXATTR4res();
                break;
            case nfs_opnum4.OP_SETXATTR:
                resop.opsetxattr = new SETXATTR4res();
                break;
            case nfs_opnum4.OP_LISTXATTRS:
                resop.oplistxattrs = new LISTXATTRS4res();
                break;
            case nfs_opnum4.OP_REMOVEXATTR:
                resop.opremovexattr = new REMOVEXATTR4res();
                break;
            case nfs_opnum4.OP_ILLEGAL:
                resop.opillegal = new ILLEGAL4res();
                break;
            default:
                resop.resop = nfs_opnum4.OP_ILLEGAL;
                resop.opillegal = new ILLEGAL4res();
        }
        return resop;
    }
}
// End of nfs_resop4.java
