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

import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class nfs_cb_resop4 implements XdrAble {
    public int resop;
    public CB_GETATTR4res opcbgetattr;
    public CB_RECALL4res opcbrecall;
    public CB_LAYOUTRECALL4res opcblayoutrecall;
    public CB_NOTIFY4res opcbnotify;
    public CB_PUSH_DELEG4res opcbpush_deleg;
    public CB_RECALL_ANY4res opcbrecall_any;
    public CB_RECALLABLE_OBJ_AVAIL4res opcbrecallable_obj_avail;
    public CB_RECALL_SLOT4res opcbrecall_slot;
    public CB_SEQUENCE4res opcbsequence;
    public CB_WANTS_CANCELLED4res opcbwants_cancelled;
    public CB_NOTIFY_LOCK4res opcbnotify_lock;
    public CB_NOTIFY_DEVICEID4res opcbnotify_deviceid;
    public CB_OFFLOAD4res opcboffload;
    public CB_ILLEGAL4res opcbillegal;

    public nfs_cb_resop4() {
    }

    public nfs_cb_resop4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(resop);
        switch (resop) {
            case nfs_cb_opnum4.OP_CB_GETATTR:
                opcbgetattr.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALL:
                opcbrecall.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_LAYOUTRECALL:
                opcblayoutrecall.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_NOTIFY:
                opcbnotify.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_PUSH_DELEG:
                opcbpush_deleg.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALL_ANY:
                opcbrecall_any.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALLABLE_OBJ_AVAIL:
                opcbrecallable_obj_avail.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALL_SLOT:
                opcbrecall_slot.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_SEQUENCE:
                opcbsequence.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_WANTS_CANCELLED:
                opcbwants_cancelled.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_NOTIFY_LOCK:
                opcbnotify_lock.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_NOTIFY_DEVICEID:
                opcbnotify_deviceid.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_OFFLOAD:
                opcboffload.xdrEncode(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_ILLEGAL:
                opcbillegal.xdrEncode(xdr);
                break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        resop = xdr.xdrDecodeInt();
        switch (resop) {
            case nfs_cb_opnum4.OP_CB_GETATTR:
                opcbgetattr = new CB_GETATTR4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALL:
                opcbrecall = new CB_RECALL4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_LAYOUTRECALL:
                opcblayoutrecall = new CB_LAYOUTRECALL4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_NOTIFY:
                opcbnotify = new CB_NOTIFY4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_PUSH_DELEG:
                opcbpush_deleg = new CB_PUSH_DELEG4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALL_ANY:
                opcbrecall_any = new CB_RECALL_ANY4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALLABLE_OBJ_AVAIL:
                opcbrecallable_obj_avail = new CB_RECALLABLE_OBJ_AVAIL4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_RECALL_SLOT:
                opcbrecall_slot = new CB_RECALL_SLOT4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_SEQUENCE:
                opcbsequence = new CB_SEQUENCE4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_WANTS_CANCELLED:
                opcbwants_cancelled = new CB_WANTS_CANCELLED4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_NOTIFY_LOCK:
                opcbnotify_lock = new CB_NOTIFY_LOCK4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_NOTIFY_DEVICEID:
                opcbnotify_deviceid = new CB_NOTIFY_DEVICEID4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_OFFLOAD:
                opcboffload = new CB_OFFLOAD4res(xdr);
                break;
            case nfs_cb_opnum4.OP_CB_ILLEGAL:
                opcbillegal = new CB_ILLEGAL4res(xdr);
                break;
        }
    }

}
// End of nfs_cb_resop4.java
