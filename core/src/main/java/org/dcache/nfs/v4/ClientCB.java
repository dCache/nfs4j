/*
 * Copyright (c) 2009 - 2024 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.CB_COMPOUND4args;
import org.dcache.nfs.v4.xdr.CB_COMPOUND4res;
import org.dcache.nfs.v4.xdr.CB_LAYOUTRECALL4args;
import org.dcache.nfs.v4.xdr.CB_NOTIFY_DEVICEID4args;
import org.dcache.nfs.v4.xdr.CB_OFFLOAD4args;
import org.dcache.nfs.v4.xdr.CB_RECALL4args;
import org.dcache.nfs.v4.xdr.CB_SEQUENCE4args;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.callback_sec_parms4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.fsid4;
import org.dcache.nfs.v4.xdr.layoutiomode4;
import org.dcache.nfs.v4.xdr.layoutrecall4;
import org.dcache.nfs.v4.xdr.layoutrecall_file4;
import org.dcache.nfs.v4.xdr.layoutrecall_type4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_cb_argop4;
import org.dcache.nfs.v4.xdr.nfs_cb_opnum4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.notify4;
import org.dcache.nfs.v4.xdr.notify_deviceid_delete4;
import org.dcache.nfs.v4.xdr.notify_deviceid_type4;
import org.dcache.nfs.v4.xdr.notifylist4;
import org.dcache.nfs.v4.xdr.offload_info4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.referring_call_list4;
import org.dcache.nfs.v4.xdr.sessionid4;
import org.dcache.nfs.v4.xdr.slotid4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.uint64_t;
import org.dcache.nfs.v4.xdr.utf8str_cs;
import org.dcache.nfs.v4.xdr.write_response4;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcAuthType;
import org.dcache.oncrpc4j.rpc.RpcAuthTypeNone;
import org.dcache.oncrpc4j.rpc.RpcAuthTypeUnix;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;
import org.dcache.oncrpc4j.xdr.Xdr;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrVoid;

/**
 * A class to provide callbacks to the client.
 */
public class ClientCB {

    /**
     * call-back rpc program version number.
     * cross-vendor agreed to have '1'.
     */
    private static final int CB_VERSION = 1;
    private final sessionid4 _session;

    /**
     * Minor version of the nfs protocol used by client.
     */
    private final int _minorVersion;
    /**
     * authentication flavor to be used for call-backs
     */
    private final RpcAuth _auth;

    /**
     * The highest slot id that can be used.
     */
    private final int _highestSlotId;

    /**
     * rpc call to use to communicate with client
     */
    private final RpcCall _rpc;


    /** Session associated with this callback channel */
    private final ClientSession _clientSession;

    /**
     * @param transport for call-back communication
     * @param minorVersion nfs4 protocol minor version used by client.
     * @param session associated with the client
     * @param maxrequests the maximum number of concurrent requests.
     * @param program RPC program number to use
     * @param sec_parms supported security flavors
     */
    ClientCB(RpcTransport transport, int program, int minorVersion,  sessionid4 session, int maxrequests,
            callback_sec_parms4[] sec_parms) {
        _minorVersion = minorVersion;
        _session = session;

        switch (sec_parms[0].cb_secflavor) {
            case RpcAuthType.NONE:
                _auth = new RpcAuthTypeNone();
                break;
            case RpcAuthType.UNIX:
                _auth = new RpcAuthTypeUnix(
                        sec_parms[0].cbsp_sys_cred.uid,
                        sec_parms[0].cbsp_sys_cred.gid,
                        sec_parms[0].cbsp_sys_cred.gids,
                        sec_parms[0].cbsp_sys_cred.stamp,
                        sec_parms[0].cbsp_sys_cred.machinename);
                break;
            default:
                throw new IllegalArgumentException("Unsuppotred security flavor");
        }

        _highestSlotId = maxrequests - 1;
        _clientSession = new ClientSession(session, maxrequests);
        _rpc = new RpcCall(program, CB_VERSION, _auth, transport);
    }

    @Override
    public String toString() {
        return "tcp:///" + _rpc;
    }

    public void cbPing() throws OncRpcException, IOException, TimeoutException {
        _rpc.call(nfs4_prot.CB_NULL_1, XdrVoid.XDR_VOID, XdrVoid.XDR_VOID, 1, TimeUnit.SECONDS);
    }

    private XdrAble generateCompound(ClientSession.SessionSlot sessionSlot, String tag, nfs_cb_argop4...cbOperations) {

        CB_SEQUENCE4args cbSequence = new CB_SEQUENCE4args();
        cbSequence.csa_cachethis = false;
        cbSequence.csa_highest_slotid = new slotid4(_highestSlotId);
        cbSequence.csa_sequenceid = sessionSlot.nextSequenceId();
        cbSequence.csa_slotid = sessionSlot.getId();
        cbSequence.csa_sessionid = _session;
        cbSequence.csa_referring_call_lists = new referring_call_list4[0];

        CB_COMPOUND4args cbCompound = new CB_COMPOUND4args();
        cbCompound.argarray = new nfs_cb_argop4[cbOperations.length + 1];

        cbCompound.argarray[0] = new nfs_cb_argop4();
        cbCompound.argarray[0].argop = nfs_cb_opnum4.OP_CB_SEQUENCE;
        cbCompound.argarray[0].opcbsequence = cbSequence;

        System.arraycopy(cbOperations, 0, cbCompound.argarray, 1, cbOperations.length);

        cbCompound.minorversion = new uint32_t(_minorVersion);
        cbCompound.callback_ident = new uint32_t(0);
        cbCompound.tag = new utf8str_cs(tag);

        return cbCompound;
    }

    public void cbLayoutRecallFsid() throws OncRpcException, IOException {

        CB_LAYOUTRECALL4args cbLayoutrecall = new CB_LAYOUTRECALL4args();
        cbLayoutrecall.clora_changed = true;
        cbLayoutrecall.clora_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        cbLayoutrecall.clora_iomode = layoutiomode4.LAYOUTIOMODE4_ANY;
        cbLayoutrecall.clora_recall = new layoutrecall4();
        cbLayoutrecall.clora_recall.lor_recalltype = layoutrecall_type4.LAYOUTRECALL4_FSID;
        cbLayoutrecall.clora_recall.lor_fsid = new fsid4();
        cbLayoutrecall.clora_recall.lor_fsid.major = new uint64_t(17);
        cbLayoutrecall.clora_recall.lor_fsid.minor = new uint64_t(17);

        nfs_cb_argop4 opArgs = new nfs_cb_argop4();
        opArgs.argop = nfs_cb_opnum4.OP_CB_LAYOUTRECALL;
        opArgs.opcblayoutrecall = cbLayoutrecall;

        var slot = _clientSession.acquireSlot();
        try {
            XdrAble args = generateCompound(slot,"cb_layout_recall_fs", opArgs);
            _rpc.call(nfs4_prot.CB_COMPOUND_1, args, new CB_COMPOUND4res());
        } finally {
            _clientSession.releaseSlot(slot);
        }
    }

    /**
     * Recall file delegation from the client.
     *
     * @param fh file handle of the file
     * @param stateid stateid of the delegation
     * @param truncate true if the client should truncate the file
     * @throws OncRpcException if an RPC error occurs
     * @throws IOException if an I/O error occurs
     */
    public void cbDelegationRecall(nfs_fh4 fh, stateid4 stateid, boolean truncate) throws OncRpcException, IOException {

        CB_RECALL4args cbDelegRecall = new CB_RECALL4args();

        cbDelegRecall.stateid = stateid;
        cbDelegRecall.fh = fh;
        cbDelegRecall.truncate = truncate;

        nfs_cb_argop4 opArgs = new nfs_cb_argop4();
        opArgs.argop = nfs_cb_opnum4.OP_CB_RECALL;
        opArgs.opcbrecall = cbDelegRecall;

        var slot = _clientSession.acquireSlot();
        try {
            XdrAble args = generateCompound(slot,"cb_recall_delegation", opArgs);
            _rpc.call(nfs4_prot.CB_COMPOUND_1, args, new CB_COMPOUND4res());
        } finally {
            _clientSession.releaseSlot(slot);
        }
    }

    /**
     * Recall pNFS layout from the client.
     *
     * @param fh file handle of the file
     * @param stateid stateid of the layout
     * @throws OncRpcException if an RPC error occurs
     * @throws IOException if an I/O error occurs
     */
    public void cbLayoutRecallFile(nfs_fh4 fh, stateid4 stateid) throws OncRpcException, IOException {

        CB_LAYOUTRECALL4args cbLayoutrecall = new CB_LAYOUTRECALL4args();
        cbLayoutrecall.clora_changed = true;
        cbLayoutrecall.clora_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        cbLayoutrecall.clora_iomode = layoutiomode4.LAYOUTIOMODE4_ANY;
        cbLayoutrecall.clora_recall = new layoutrecall4();
        cbLayoutrecall.clora_recall.lor_recalltype = layoutrecall_type4.LAYOUTRECALL4_FILE;
        cbLayoutrecall.clora_recall.lor_layout = new layoutrecall_file4();
        cbLayoutrecall.clora_recall.lor_layout.lor_fh = fh;
        cbLayoutrecall.clora_recall.lor_layout.lor_offset = new offset4(0L);
        cbLayoutrecall.clora_recall.lor_layout.lor_length = new length4(nfs4_prot.NFS4_UINT64_MAX);
        cbLayoutrecall.clora_recall.lor_layout.lor_stateid = stateid;

        nfs_cb_argop4 opArgs = new nfs_cb_argop4();
        opArgs.argop = nfs_cb_opnum4.OP_CB_LAYOUTRECALL;
        opArgs.opcblayoutrecall = cbLayoutrecall;

        var slot = _clientSession.acquireSlot();
        try{
            XdrAble args = generateCompound(slot,"cb_layout_recall_file", opArgs);

            CB_COMPOUND4res res = new CB_COMPOUND4res();
            _rpc.call(nfs4_prot.CB_COMPOUND_1, args, res);
            nfsstat.throwIfNeeded(res.status);
        } finally {
            _clientSession.releaseSlot(slot);
        }
    }

    /**
     * Notify the client that a device id has been added.
     *
     * @param id device id
     * @throws OncRpcException if an RPC error occurs
     * @throws IOException if an I/O error occurs
     */
    public void cbDeleteDevice(deviceid4 id) throws OncRpcException, IOException {

        CB_NOTIFY_DEVICEID4args cbDeleteDeciveId = new CB_NOTIFY_DEVICEID4args();

        cbDeleteDeciveId.cnda_changes = new notify4[1];
        cbDeleteDeciveId.cnda_changes[0] = new notify4();
        cbDeleteDeciveId.cnda_changes[0].notify_mask = bitmap4.of(notify_deviceid_type4.NOTIFY_DEVICEID4_DELETE);

        notify_deviceid_delete4 deleteDevice = new notify_deviceid_delete4();
        deleteDevice.ndd_layouttype = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        deleteDevice.ndd_deviceid = id;

        try (Xdr xdr = new Xdr(32)) {

            xdr.beginEncoding();
            deleteDevice.xdrEncode(xdr);
            xdr.endEncoding();
            byte[] b = xdr.getBytes();

            cbDeleteDeciveId.cnda_changes[0].notify_vals = new notifylist4(b);
        }

        nfs_cb_argop4 opArgs = new nfs_cb_argop4();
        opArgs.argop = nfs_cb_opnum4.OP_CB_NOTIFY_DEVICEID;
        opArgs.opcbnotify_deviceid = cbDeleteDeciveId;

        var slot = _clientSession.acquireSlot();
        try{
            XdrAble args = generateCompound(slot,"cb_delete_device", opArgs);

            CB_COMPOUND4res res = new CB_COMPOUND4res();
            _rpc.call(nfs4_prot.CB_COMPOUND_1, args, res);
            nfsstat.throwIfNeeded(res.status);
        } finally {
            _clientSession.releaseSlot(slot);
        }
    }

    /**
     * Notify the client that offload copy has been completed.
     *
     * @param fh       file handle of the file
     * @param stateid  stateid of the file
     * @param response write response
     * @param status   status of the offload
     */
    public void cbOffload(nfs_fh4 fh, stateid4 stateid, write_response4 response, int status) throws OncRpcException, IOException {

        CB_OFFLOAD4args copyOffload = new CB_OFFLOAD4args();

        copyOffload.coa_fh = fh;
        copyOffload.coa_stateid = stateid;
        copyOffload.coa_offload_info = new offload_info4();
        copyOffload.coa_offload_info.coa_resok4 = response;
        copyOffload.coa_offload_info.coa_status = status;

        nfs_cb_argop4 opArgs = new nfs_cb_argop4();
        opArgs.argop = nfs_cb_opnum4.OP_CB_OFFLOAD;
        opArgs.opcboffload = copyOffload;

        var slot = _clientSession.acquireSlot();
        try{
            XdrAble args = generateCompound(slot,"cb_offload", opArgs);

            CB_COMPOUND4res res = new CB_COMPOUND4res();
            _rpc.call(nfs4_prot.CB_COMPOUND_1, args, res);
            nfsstat.throwIfNeeded(res.status);
        } finally {
            _clientSession.releaseSlot(slot);
        }
    }


}
