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

import org.dcache.nfs.v4.xdr.ACCESS4args;
import org.dcache.nfs.v4.xdr.BACKCHANNEL_CTL4args;
import org.dcache.nfs.v4.xdr.BIND_CONN_TO_SESSION4args;
import org.dcache.nfs.v4.xdr.CLOSE4args;
import org.dcache.nfs.v4.xdr.COMMIT4args;
import org.dcache.nfs.v4.xdr.CREATE4args;
import org.dcache.nfs.v4.xdr.CREATE_SESSION4args;
import org.dcache.nfs.v4.xdr.DELEGPURGE4args;
import org.dcache.nfs.v4.xdr.DELEGRETURN4args;
import org.dcache.nfs.v4.xdr.DESTROY_CLIENTID4args;
import org.dcache.nfs.v4.xdr.DESTROY_SESSION4args;
import org.dcache.nfs.v4.xdr.EXCHANGE_ID4args;
import org.dcache.nfs.v4.xdr.FREE_STATEID4args;
import org.dcache.nfs.v4.xdr.GETATTR4args;
import org.dcache.nfs.v4.xdr.GETDEVICEINFO4args;
import org.dcache.nfs.v4.xdr.GETDEVICELIST4args;
import org.dcache.nfs.v4.xdr.GET_DIR_DELEGATION4args;
import org.dcache.nfs.v4.xdr.LAYOUTCOMMIT4args;
import org.dcache.nfs.v4.xdr.LAYOUTGET4args;
import org.dcache.nfs.v4.xdr.LAYOUTRETURN4args;
import org.dcache.nfs.v4.xdr.LINK4args;
import org.dcache.nfs.v4.xdr.LOCK4args;
import org.dcache.nfs.v4.xdr.LOCKT4args;
import org.dcache.nfs.v4.xdr.LOCKU4args;
import org.dcache.nfs.v4.xdr.LOOKUP4args;
import org.dcache.nfs.v4.xdr.NVERIFY4args;
import org.dcache.nfs.v4.xdr.OPEN4args;
import org.dcache.nfs.v4.xdr.OPENATTR4args;
import org.dcache.nfs.v4.xdr.OPEN_CONFIRM4args;
import org.dcache.nfs.v4.xdr.OPEN_DOWNGRADE4args;
import org.dcache.nfs.v4.xdr.PUTFH4args;
import org.dcache.nfs.v4.xdr.READ4args;
import org.dcache.nfs.v4.xdr.READDIR4args;
import org.dcache.nfs.v4.xdr.RECLAIM_COMPLETE4args;
import org.dcache.nfs.v4.xdr.RELEASE_LOCKOWNER4args;
import org.dcache.nfs.v4.xdr.REMOVE4args;
import org.dcache.nfs.v4.xdr.RENAME4args;
import org.dcache.nfs.v4.xdr.RENEW4args;
import org.dcache.nfs.v4.xdr.SECINFO4args;
import org.dcache.nfs.v4.xdr.SECINFO_NO_NAME4args;
import org.dcache.nfs.v4.xdr.SEQUENCE4args;
import org.dcache.nfs.v4.xdr.SETATTR4args;
import org.dcache.nfs.v4.xdr.SETCLIENTID4args;
import org.dcache.nfs.v4.xdr.SETCLIENTID_CONFIRM4args;
import org.dcache.nfs.v4.xdr.SET_SSV4args;
import org.dcache.nfs.v4.xdr.TEST_STATEID4args;
import org.dcache.nfs.v4.xdr.VERIFY4args;
import org.dcache.nfs.v4.xdr.WANT_DELEGATION4args;
import org.dcache.nfs.v4.xdr.WRITE4args;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class nfs_argop4 implements XdrAble {
    public int argop;
    public ACCESS4args opaccess;
    public CLOSE4args opclose;
    public COMMIT4args opcommit;
    public CREATE4args opcreate;
    public DELEGPURGE4args opdelegpurge;
    public DELEGRETURN4args opdelegreturn;
    public GETATTR4args opgetattr;
    public LINK4args oplink;
    public LOCK4args oplock;
    public LOCKT4args oplockt;
    public LOCKU4args oplocku;
    public LOOKUP4args oplookup;
    public NVERIFY4args opnverify;
    public OPEN4args opopen;
    public OPENATTR4args opopenattr;
    public OPEN_CONFIRM4args opopen_confirm;
    public OPEN_DOWNGRADE4args opopen_downgrade;
    public PUTFH4args opputfh;
    public READ4args opread;
    public READDIR4args opreaddir;
    public REMOVE4args opremove;
    public RENAME4args oprename;
    public RENEW4args oprenew;
    public SECINFO4args opsecinfo;
    public SETATTR4args opsetattr;
    public SETCLIENTID4args opsetclientid;
    public SETCLIENTID_CONFIRM4args opsetclientid_confirm;
    public VERIFY4args opverify;
    public WRITE4args opwrite;
    public RELEASE_LOCKOWNER4args oprelease_lockowner;
    public BACKCHANNEL_CTL4args opbackchannel_ctl;
    public BIND_CONN_TO_SESSION4args opbind_conn_to_session;
    public EXCHANGE_ID4args opexchange_id;
    public CREATE_SESSION4args opcreate_session;
    public DESTROY_SESSION4args opdestroy_session;
    public FREE_STATEID4args opfree_stateid;
    public GET_DIR_DELEGATION4args opget_dir_delegation;
    public GETDEVICEINFO4args opgetdeviceinfo;
    public GETDEVICELIST4args opgetdevicelist;
    public LAYOUTCOMMIT4args oplayoutcommit;
    public LAYOUTERROR4args oplayouterror;
    public LAYOUTGET4args oplayoutget;
    public LAYOUTRETURN4args oplayoutreturn;
    public LAYOUTSTATS4args oplayoutstats;
    public SECINFO_NO_NAME4args opsecinfo_no_name;
    public SEQUENCE4args opsequence;
    public SET_SSV4args opset_ssv;
    public TEST_STATEID4args optest_stateid;
    public WANT_DELEGATION4args opwant_delegation;
    public DESTROY_CLIENTID4args opdestroy_clientid;
    public RECLAIM_COMPLETE4args opreclaim_complete;
    public ALLOCATE4args opallocate;
    public COPY4args opcopy;
    public COPY_NOTIFY4args opoffload_notify;
    public DEALLOCATE4args opdeallocate;
    public IO_ADVISE4args opio_advise;
    public OFFLOAD_CANCEL4args opoffload_cancel;
    public OFFLOAD_STATUS4args opoffload_status;
    public READ_PLUS4args opread_plus;
    public SEEK4args opseek;
    public WRITE_SAME4args opwrite_same;
    public CLONE4args opclone;
    public GETXATTR4args opgetxattr;
    public SETXATTR4args opsetxattr;
    public LISTXATTRS4args oplistxattrs;
    public REMOVEXATTR4args opremovexattr;

    public nfs_argop4() {
    }

    public nfs_argop4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(argop);
        switch (argop) {
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
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                break;
            case nfs_opnum4.OP_READ:
                opread.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_READDIR:
                opreaddir.xdrEncode(xdr);
                break;
            case nfs_opnum4.OP_READLINK:
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
                break;
            case nfs_opnum4.OP_SAVEFH:
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
                opoffload_notify.xdrEncode(xdr);
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
                break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        argop = xdr.xdrDecodeInt();
        switch (argop) {
            case nfs_opnum4.OP_ACCESS:
                opaccess = new ACCESS4args(xdr);
                break;
            case nfs_opnum4.OP_CLOSE:
                opclose = new CLOSE4args(xdr);
                break;
            case nfs_opnum4.OP_COMMIT:
                opcommit = new COMMIT4args(xdr);
                break;
            case nfs_opnum4.OP_CREATE:
                opcreate = new CREATE4args(xdr);
                break;
            case nfs_opnum4.OP_DELEGPURGE:
                opdelegpurge = new DELEGPURGE4args(xdr);
                break;
            case nfs_opnum4.OP_DELEGRETURN:
                opdelegreturn = new DELEGRETURN4args(xdr);
                break;
            case nfs_opnum4.OP_GETATTR:
                opgetattr = new GETATTR4args(xdr);
                break;
            case nfs_opnum4.OP_GETFH:
                break;
            case nfs_opnum4.OP_LINK:
                oplink = new LINK4args(xdr);
                break;
            case nfs_opnum4.OP_LOCK:
                oplock = new LOCK4args(xdr);
                break;
            case nfs_opnum4.OP_LOCKT:
                oplockt = new LOCKT4args(xdr);
                break;
            case nfs_opnum4.OP_LOCKU:
                oplocku = new LOCKU4args(xdr);
                break;
            case nfs_opnum4.OP_LOOKUP:
                oplookup = new LOOKUP4args(xdr);
                break;
            case nfs_opnum4.OP_LOOKUPP:
                break;
            case nfs_opnum4.OP_NVERIFY:
                opnverify = new NVERIFY4args(xdr);
                break;
            case nfs_opnum4.OP_OPEN:
                opopen = new OPEN4args(xdr);
                break;
            case nfs_opnum4.OP_OPENATTR:
                opopenattr = new OPENATTR4args(xdr);
                break;
            case nfs_opnum4.OP_OPEN_CONFIRM:
                opopen_confirm = new OPEN_CONFIRM4args(xdr);
                break;
            case nfs_opnum4.OP_OPEN_DOWNGRADE:
                opopen_downgrade = new OPEN_DOWNGRADE4args(xdr);
                break;
            case nfs_opnum4.OP_PUTFH:
                opputfh = new PUTFH4args(xdr);
                break;
            case nfs_opnum4.OP_PUTPUBFH:
                break;
            case nfs_opnum4.OP_PUTROOTFH:
                break;
            case nfs_opnum4.OP_READ:
                opread = new READ4args(xdr);
                break;
            case nfs_opnum4.OP_READDIR:
                opreaddir = new READDIR4args(xdr);
                break;
            case nfs_opnum4.OP_READLINK:
                break;
            case nfs_opnum4.OP_REMOVE:
                opremove = new REMOVE4args(xdr);
                break;
            case nfs_opnum4.OP_RENAME:
                oprename = new RENAME4args(xdr);
                break;
            case nfs_opnum4.OP_RENEW:
                oprenew = new RENEW4args(xdr);
                break;
            case nfs_opnum4.OP_RESTOREFH:
                break;
            case nfs_opnum4.OP_SAVEFH:
                break;
            case nfs_opnum4.OP_SECINFO:
                opsecinfo = new SECINFO4args(xdr);
                break;
            case nfs_opnum4.OP_SETATTR:
                opsetattr = new SETATTR4args(xdr);
                break;
            case nfs_opnum4.OP_SETCLIENTID:
                opsetclientid = new SETCLIENTID4args(xdr);
                break;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
                opsetclientid_confirm = new SETCLIENTID_CONFIRM4args(xdr);
                break;
            case nfs_opnum4.OP_VERIFY:
                opverify = new VERIFY4args(xdr);
                break;
            case nfs_opnum4.OP_WRITE:
                opwrite = new WRITE4args(xdr);
                break;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
                oprelease_lockowner = new RELEASE_LOCKOWNER4args(xdr);
                break;
            case nfs_opnum4.OP_BACKCHANNEL_CTL:
                opbackchannel_ctl = new BACKCHANNEL_CTL4args(xdr);
                break;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                opbind_conn_to_session = new BIND_CONN_TO_SESSION4args(xdr);
                break;
            case nfs_opnum4.OP_EXCHANGE_ID:
                opexchange_id = new EXCHANGE_ID4args(xdr);
                break;
            case nfs_opnum4.OP_CREATE_SESSION:
                opcreate_session = new CREATE_SESSION4args(xdr);
                break;
            case nfs_opnum4.OP_DESTROY_SESSION:
                opdestroy_session = new DESTROY_SESSION4args(xdr);
                break;
            case nfs_opnum4.OP_FREE_STATEID:
                opfree_stateid = new FREE_STATEID4args(xdr);
                break;
            case nfs_opnum4.OP_GET_DIR_DELEGATION:
                opget_dir_delegation = new GET_DIR_DELEGATION4args(xdr);
                break;
            case nfs_opnum4.OP_GETDEVICEINFO:
                opgetdeviceinfo = new GETDEVICEINFO4args(xdr);
                break;
            case nfs_opnum4.OP_GETDEVICELIST:
                opgetdevicelist = new GETDEVICELIST4args(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTCOMMIT:
                oplayoutcommit = new LAYOUTCOMMIT4args(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTERROR:
                oplayouterror = new LAYOUTERROR4args(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTGET:
                oplayoutget = new LAYOUTGET4args(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTRETURN:
                oplayoutreturn = new LAYOUTRETURN4args(xdr);
                break;
            case nfs_opnum4.OP_LAYOUTSTATS:
                oplayoutstats = new LAYOUTSTATS4args(xdr);
                break;
            case nfs_opnum4.OP_SECINFO_NO_NAME:
                opsecinfo_no_name = new SECINFO_NO_NAME4args(xdr);
                break;
            case nfs_opnum4.OP_SEQUENCE:
                opsequence = new SEQUENCE4args(xdr);
                break;
            case nfs_opnum4.OP_SET_SSV:
                opset_ssv = new SET_SSV4args(xdr);
                break;
            case nfs_opnum4.OP_TEST_STATEID:
                optest_stateid = new TEST_STATEID4args(xdr);
                break;
            case nfs_opnum4.OP_WANT_DELEGATION:
                opwant_delegation = new WANT_DELEGATION4args(xdr);
                break;
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                opdestroy_clientid = new DESTROY_CLIENTID4args(xdr);
                break;
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                opreclaim_complete = new RECLAIM_COMPLETE4args(xdr);
                break;
            case nfs_opnum4.OP_ALLOCATE:
                opallocate = new ALLOCATE4args(xdr);
                break;
            case nfs_opnum4.OP_COPY:
                opcopy = new COPY4args(xdr);
                break;
            case nfs_opnum4.OP_COPY_NOTIFY:
                opoffload_notify = new COPY_NOTIFY4args(xdr);
                break;
            case nfs_opnum4.OP_DEALLOCATE:
                opdeallocate = new DEALLOCATE4args(xdr);
                break;
            case nfs_opnum4.OP_IO_ADVISE:
                opio_advise = new IO_ADVISE4args(xdr);
                break;
            case nfs_opnum4.OP_OFFLOAD_CANCEL:
                opoffload_cancel = new OFFLOAD_CANCEL4args(xdr);
                break;
            case nfs_opnum4.OP_OFFLOAD_STATUS:
                opoffload_status = new OFFLOAD_STATUS4args(xdr);
                break;
            case nfs_opnum4.OP_READ_PLUS:
                opread_plus = new READ_PLUS4args(xdr);
                break;
            case nfs_opnum4.OP_SEEK:
                opseek = new SEEK4args(xdr);
                break;
            case nfs_opnum4.OP_WRITE_SAME:
                opwrite_same = new WRITE_SAME4args(xdr);
                break;
            case nfs_opnum4.OP_CLONE:
                opclone = new CLONE4args(xdr);
                break;
            case nfs_opnum4.OP_GETXATTR:
                opgetxattr = new GETXATTR4args(xdr);
                break;
            case nfs_opnum4.OP_SETXATTR:
                opsetxattr = new SETXATTR4args(xdr);
                break;
            case nfs_opnum4.OP_LISTXATTRS:
                oplistxattrs = new LISTXATTRS4args(xdr);
                break;
            case nfs_opnum4.OP_REMOVEXATTR:
                opremovexattr = new REMOVEXATTR4args(xdr);
                break;
            case nfs_opnum4.OP_ILLEGAL:
                break;
        }
    }

}
// End of nfs_argop4.java
