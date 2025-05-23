/*
 * Copyright (c) 2009 - 2021 Deutsches Elektronen-Synchroton,
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

import com.google.common.base.Splitter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import org.dcache.nfs.v4.xdr.CLOSE4args;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COPY4args;
import org.dcache.nfs.v4.xdr.CREATE4args;
import org.dcache.nfs.v4.xdr.CREATE_SESSION4args;
import org.dcache.nfs.v4.xdr.DESTROY_CLIENTID4args;
import org.dcache.nfs.v4.xdr.DESTROY_SESSION4args;
import org.dcache.nfs.v4.xdr.EXCHANGE_ID4args;
import org.dcache.nfs.v4.xdr.GETATTR4args;
import org.dcache.nfs.v4.xdr.GETDEVICEINFO4args;
import org.dcache.nfs.v4.xdr.GETDEVICELIST4args;
import org.dcache.nfs.v4.xdr.GETXATTR4args;
import org.dcache.nfs.v4.xdr.LAYOUTCOMMIT4args;
import org.dcache.nfs.v4.xdr.LAYOUTERROR4args;
import org.dcache.nfs.v4.xdr.LAYOUTGET4args;
import org.dcache.nfs.v4.xdr.LAYOUTRETURN4args;
import org.dcache.nfs.v4.xdr.LAYOUTSTATS4args;
import org.dcache.nfs.v4.xdr.LISTXATTRS4args;
import org.dcache.nfs.v4.xdr.LOCKU4args;
import org.dcache.nfs.v4.xdr.LOOKUP4args;
import org.dcache.nfs.v4.xdr.OPEN4args;
import org.dcache.nfs.v4.xdr.PUTFH4args;
import org.dcache.nfs.v4.xdr.READ4args;
import org.dcache.nfs.v4.xdr.READDIR4args;
import org.dcache.nfs.v4.xdr.RECLAIM_COMPLETE4args;
import org.dcache.nfs.v4.xdr.REMOVE4args;
import org.dcache.nfs.v4.xdr.REMOVEXATTR4args;
import org.dcache.nfs.v4.xdr.SEQUENCE4args;
import org.dcache.nfs.v4.xdr.SETXATTR4args;
import org.dcache.nfs.v4.xdr.WRITE4args;
import org.dcache.nfs.v4.xdr.attrlist4;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.callback_sec_parms4;
import org.dcache.nfs.v4.xdr.channel_attrs4;
import org.dcache.nfs.v4.xdr.client_owner4;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.component4;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.createhow4;
import org.dcache.nfs.v4.xdr.createmode4;
import org.dcache.nfs.v4.xdr.createtype4;
import org.dcache.nfs.v4.xdr.device_error4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.fattr4;
import org.dcache.nfs.v4.xdr.fattr4_mode;
import org.dcache.nfs.v4.xdr.fattr4_size;
import org.dcache.nfs.v4.xdr.io_info4;
import org.dcache.nfs.v4.xdr.layoutiomode4;
import org.dcache.nfs.v4.xdr.layoutreturn4;
import org.dcache.nfs.v4.xdr.layoutreturn_file4;
import org.dcache.nfs.v4.xdr.layoutreturn_type4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.layoutupdate4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.netloc4;
import org.dcache.nfs.v4.xdr.newoffset4;
import org.dcache.nfs.v4.xdr.newtime4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_cookie4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.nfs_ftype4;
import org.dcache.nfs.v4.xdr.nfs_impl_id4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfstime4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.open_claim4;
import org.dcache.nfs.v4.xdr.open_claim_type4;
import org.dcache.nfs.v4.xdr.open_owner4;
import org.dcache.nfs.v4.xdr.openflag4;
import org.dcache.nfs.v4.xdr.opentype4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.sequenceid4;
import org.dcache.nfs.v4.xdr.sessionid4;
import org.dcache.nfs.v4.xdr.slotid4;
import org.dcache.nfs.v4.xdr.stable_how4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.v4.xdr.state_protect4_a;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.utf8str_cis;
import org.dcache.nfs.v4.xdr.utf8str_cs;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.xdr.xattrvalue4;
import org.dcache.oncrpc4j.util.Bytes;
import org.dcache.oncrpc4j.xdr.Xdr;
import org.dcache.oncrpc4j.xdr.XdrAble;

public class CompoundBuilder {

    private static final String NO_TAG = "";

    private String tag = NO_TAG;
    private int minorversion = 1;
    private List<nfs_argop4> ops = new ArrayList<>();

    public CompoundBuilder withTag(String tag) {
        this.tag = tag;
        return this;
    }

    public CompoundBuilder withLookup(String path) {
        final Splitter splitter = Splitter.on('/').omitEmptyStrings();

        for (String s : splitter.split(path)) {
            if( "..".equals(s)) {
                withLookupp();
                continue;
            }
            nfs_argop4 op = new nfs_argop4();
            op.argop = nfs_opnum4.OP_LOOKUP;
            op.oplookup = new LOOKUP4args();

            op.oplookup.objname = new component4(s);
            ops.add(op);
        }

        return this;
    }

    public CompoundBuilder withPutfh(nfs_fh4 fh) {

        nfs_argop4 op = new nfs_argop4();
        op.opputfh = new PUTFH4args();
        op.opputfh.object = fh;

        op.argop = nfs_opnum4.OP_PUTFH;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withReaddir(long cookie, verifier4 verifier, int dirCount, int maxcount, int... attrs) {

        nfs_argop4 op = new nfs_argop4();
        op.opreaddir = new READDIR4args();
        op.opreaddir.cookie = new nfs_cookie4(cookie);
        op.opreaddir.dircount = new count4(dirCount);
        op.opreaddir.maxcount = new count4(maxcount);
        op.opreaddir.attr_request = attrs.length == 0 ? new bitmap4() : bitmap4.of(attrs);
        op.opreaddir.cookieverf = verifier;

        op.argop = nfs_opnum4.OP_READDIR;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetfh() {
        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_GETFH;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withExchangeId(String nii_domain,
            String nii_name, String co_ownerid, int flags, int how) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_EXCHANGE_ID;
        op.opexchange_id = new EXCHANGE_ID4args();
        op.opexchange_id.eia_client_impl_id = new nfs_impl_id4[1];
        nfs_impl_id4 n4 = new nfs_impl_id4();
        n4.nii_domain = new utf8str_cis(nii_domain);
        n4.nii_name = new utf8str_cs(nii_name);
        op.opexchange_id.eia_client_impl_id[0] = n4;

        nfstime4 releaseDate = new nfstime4(System.currentTimeMillis());

        op.opexchange_id.eia_client_impl_id[0].nii_date = releaseDate;
        op.opexchange_id.eia_clientowner = new client_owner4();
        op.opexchange_id.eia_clientowner.co_ownerid = co_ownerid.getBytes(StandardCharsets.UTF_8);

        op.opexchange_id.eia_clientowner.co_verifier = new verifier4();
        op.opexchange_id.eia_clientowner.co_verifier.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];
        Bytes.putLong(op.opexchange_id.eia_clientowner.co_verifier.value, 0, releaseDate.seconds);

        op.opexchange_id.eia_flags = new uint32_t(flags);
        op.opexchange_id.eia_state_protect = new state_protect4_a();
        op.opexchange_id.eia_state_protect.spa_how = how;

        ops.add(op);
        return this;
    }

    public CompoundBuilder withPutrootfh() {
        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_PUTROOTFH;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withLookupp() {
        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_LOOKUPP;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withMinorversion(int minorversion) {
        this.minorversion = minorversion;
        return this;
    }

    public CompoundBuilder withCreatesession(clientid4 clientid, sequenceid4 sequenceid) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_CREATE_SESSION;
        op.opcreate_session = new CREATE_SESSION4args();

        channel_attrs4 chan_attrs = new channel_attrs4();
        chan_attrs.ca_headerpadsize = new count4(0);
        chan_attrs.ca_maxoperations = new count4(8192);
        chan_attrs.ca_maxrequests = new count4(8192);
        chan_attrs.ca_maxrequestsize = new count4(8192);
        chan_attrs.ca_maxresponsesize = new count4(8192);
        chan_attrs.ca_maxresponsesize_cached = new count4(8);
        chan_attrs.ca_rdma_ird = new uint32_t[0];

        op.opcreate_session.csa_clientid = clientid;
        op.opcreate_session.csa_sequence = sequenceid;
        op.opcreate_session.csa_flags = new uint32_t(nfs4_prot.CREATE_SESSION4_FLAG_PERSIST);
        op.opcreate_session.csa_fore_chan_attrs = chan_attrs;
        op.opcreate_session.csa_back_chan_attrs = chan_attrs;
        op.opcreate_session.csa_cb_program = new uint32_t(123);

        op.opcreate_session.csa_sec_parms = new callback_sec_parms4[1];
        op.opcreate_session.csa_sec_parms[0] = new callback_sec_parms4();
        op.opcreate_session.csa_sec_parms[0].cb_secflavor = nfs4_prot.AUTH_NONE;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withSequence(boolean CacheThis, sessionid4 sessionid,
            int sequence, int slotid, int highSlotid) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_SEQUENCE;
        op.opsequence = new SEQUENCE4args();
        op.opsequence.sa_cachethis = CacheThis;

        slotid4 sId = new slotid4(slotid);
        op.opsequence.sa_slotid = sId;

        slotid4 HsId = new slotid4(highSlotid);
        op.opsequence.sa_highest_slotid = HsId;
        op.opsequence.sa_sequenceid = new sequenceid4(++sequence);

        op.opsequence.sa_sessionid = sessionid;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetattr(int... attrs) {
        nfs_argop4 op = new nfs_argop4();
        GETATTR4args args = new GETATTR4args();

        args.attr_request = bitmap4.of(attrs);
        op.argop = nfs_opnum4.OP_GETATTR;
        op.opgetattr = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withSavefh() {
        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_SAVEFH;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withRestorefh() {
        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_RESTOREFH;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withDestroysession(sessionid4 sessionid) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_DESTROY_SESSION;
        op.opdestroy_session = new DESTROY_SESSION4args();

        op.opdestroy_session.dsa_sessionid = sessionid;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withDestroyclientid(clientid4 clientid) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_DESTROY_CLIENTID;
        op.opdestroy_clientid = new DESTROY_CLIENTID4args();

        op.opdestroy_clientid.dca_clientid = clientid;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withRemove(String path) {
        REMOVE4args args = new REMOVE4args();

        args.target = new component4(path);
        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_REMOVE;
        op.opremove = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withWrite(long offset, byte[] data, stateid4 stateid) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        return withWrite(offset, buf, stateid);
    }

    public CompoundBuilder withWrite(long offset, ByteBuffer data, stateid4 stateid) {
        WRITE4args args = new WRITE4args();

        args.stable = stable_how4.FILE_SYNC4;
        args.offset = new offset4(offset);
        args.stateid = stateid;
        args.data = data;

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_WRITE;
        op.opwrite = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withRead(int count, long offset, stateid4 stateid) {
        READ4args args = new READ4args();
        args.count = new count4(count);
        args.offset = new offset4(offset);

        args.stateid = stateid;

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_READ;
        op.opread = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetdevicelist(layouttype4 layoutType) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICELIST;
        op.opgetdevicelist = new GETDEVICELIST4args();

        op.opgetdevicelist.gdla_cookie = new nfs_cookie4(0);

        op.opgetdevicelist.gdla_cookieverf = new verifier4();
        op.opgetdevicelist.gdla_cookieverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        op.opgetdevicelist.gdla_layout_type = layoutType.getValue();
        op.opgetdevicelist.gdla_maxdevices = new count4(256);
        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetdeviceinfo(deviceid4 devId, layouttype4 layoutType) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICEINFO;
        op.opgetdeviceinfo = new GETDEVICEINFO4args();

        op.opgetdeviceinfo.gdia_device_id = devId;
        op.opgetdeviceinfo.gdia_layout_type = layoutType.getValue();

        op.opgetdeviceinfo.gdia_maxcount = new count4(1024);
        op.opgetdeviceinfo.gdia_notify_types = new bitmap4();
        op.opgetdeviceinfo.gdia_notify_types.value = new int[] {1};

        ops.add(op);
        return this;
    }

    public CompoundBuilder withLayoutreturn(long offset, long len, byte[] body, stateid4 stateid) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTRETURN;
        op.oplayoutreturn = new LAYOUTRETURN4args();

        op.oplayoutreturn.lora_reclaim = false;
        op.oplayoutreturn.lora_iomode = layoutiomode4.LAYOUTIOMODE4_ANY;
        op.oplayoutreturn.lora_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES.getValue();
        op.oplayoutreturn.lora_layoutreturn = new layoutreturn4();
        op.oplayoutreturn.lora_layoutreturn.lr_returntype = layoutreturn_type4.LAYOUTRETURN4_FILE;
        op.oplayoutreturn.lora_layoutreturn.lr_layout = new layoutreturn_file4();
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_offset = new offset4(offset);
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_length = new length4(len);
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_body = body;
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_stateid = stateid;

        ops.add(op);
        return this;
    }

    public CompoundBuilder withClose(stateid4 stateid, int seq) {
        CLOSE4args args = new CLOSE4args();

        args.seqid = new seqid4(seq);
        args.open_stateid = stateid;

        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_CLOSE;
        op.opclose = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withOpenCreate(String path, int sequenceId, clientid4 clientid, int access) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_OPEN;
        op.opopen = new OPEN4args();

        op.opopen.seqid = new seqid4(sequenceId);

        state_owner4 owner = new state_owner4();
        owner.clientid = clientid;
        owner.owner = "JUnitChimera".getBytes(StandardCharsets.UTF_8);
        op.opopen.owner = new open_owner4(owner);

        if ((access & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_DELEG_MASK) == 0) {
            access |= nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        }
        op.opopen.share_access = new uint32_t(access);
        op.opopen.share_deny = new uint32_t(nfs4_prot.OPEN4_SHARE_DENY_NONE);

        openflag4 flag = new openflag4();
        flag.opentype = opentype4.OPEN4_CREATE;

        // createhow4(mode, attrs, verifier)
        createhow4 how = new createhow4();
        how.mode = createmode4.GUARDED4;

        fattr4 attr = new fattr4();
        attr.attr_vals = new attrlist4();
        attr.attr_vals.value = encodeAttrs(new fattr4_size(0), new fattr4_mode(0644));
        attr.attrmask = bitmap4.of(nfs4_prot.FATTR4_SIZE, nfs4_prot.FATTR4_MODE);

        how.createattrs = attr;
        how.createverf = new verifier4(new byte[nfs4_prot.NFS4_VERIFIER_SIZE]);
        how.mode = createmode4.GUARDED4;

        flag.how = how;
        op.opopen.openhow = flag;

        open_claim4 claim = new open_claim4();
        claim.claim = open_claim_type4.CLAIM_NULL;
        claim.file = new component4(path);
        claim.delegate_type = nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        claim.file_delegate_prev = null;
        claim.oc_delegate_stateid = null;
        claim.delegate_type = 0;
        claim.delegate_cur_info = null;

        op.opopen.claim = claim;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withMakedir(String path) {
        CREATE4args args = new CREATE4args();

        args.objname = new component4(path);
        args.objtype = new createtype4();
        args.objtype.type = nfs_ftype4.NF4DIR;
        args.createattrs = new fattr4();

        args.createattrs.attr_vals = new attrlist4();
        args.createattrs.attr_vals.value = encodeAttrs(new fattr4_mode(0755));
        args.createattrs.attrmask = bitmap4.of(nfs4_prot.FATTR4_MODE);

        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_CREATE;
        op.opcreate = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withOpen(String path, int sequenceId,
            clientid4 clientid, int access) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_OPEN;
        op.opopen = new OPEN4args();

        op.opopen.seqid = new seqid4(sequenceId);

        // if ((access & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_DELEG_MASK) == 0){
        // access |= nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        // }
        // op.opopen.share_access = new uint32_t(access);
        op.opopen.share_access = new uint32_t(nfs4_prot.OPEN4_SHARE_ACCESS_READ);
        op.opopen.share_deny = new uint32_t(nfs4_prot.OPEN4_SHARE_DENY_NONE);

        state_owner4 owner = new state_owner4();
        owner.clientid = clientid;
        owner.owner = "JUnitChimera".getBytes(StandardCharsets.UTF_8);
        op.opopen.owner = new open_owner4(owner);

        openflag4 flag = new openflag4();
        flag.opentype = opentype4.OPEN4_NOCREATE;

        createhow4 how = new createhow4();
        how.mode = createmode4.UNCHECKED4;
        flag.how = how;
        op.opopen.openhow = flag;

        open_claim4 claim = new open_claim4();
        claim.claim = open_claim_type4.CLAIM_NULL;
        claim.file = new component4(path);
        claim.delegate_type = nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        claim.file_delegate_prev = null;
        claim.oc_delegate_stateid = null;
        claim.delegate_type = 0;
        claim.delegate_cur_info = null;

        op.opopen.claim = claim;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withLayoutget(boolean signal_layout_avail,
            layouttype4 layout_type, int iomode, long offset, long length, long minlength,
            int maxcount, stateid4 stateid) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTGET;
        op.oplayoutget = new LAYOUTGET4args();

        op.oplayoutget.loga_signal_layout_avail = signal_layout_avail;
        op.oplayoutget.loga_layout_type = layout_type.getValue();
        op.oplayoutget.loga_iomode = iomode;

        op.oplayoutget.loga_offset = new offset4(offset);
        op.oplayoutget.loga_length = new length4(length);
        op.oplayoutget.loga_minlength = new length4(minlength);
        op.oplayoutget.loga_maxcount = new count4(maxcount);

        op.oplayoutget.loga_stateid = stateid;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withReclaimComplete() {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_RECLAIM_COMPLETE;
        op.opreclaim_complete = new RECLAIM_COMPLETE4args();

        op.opreclaim_complete.rca_one_fs = false;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withReclaimCompleteOneFs() {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_RECLAIM_COMPLETE;
        op.opreclaim_complete = new RECLAIM_COMPLETE4args();

        op.opreclaim_complete.rca_one_fs = true;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withLocku(int type, stateid4 stateid, int seqid,
            long offset, long length) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LOCKU;
        op.oplocku = new LOCKU4args();
        op.oplocku.lock_stateid = stateid;
        op.oplocku.seqid = new seqid4(seqid);
        op.oplocku.locktype = type;
        op.oplocku.offset = new offset4(offset);
        op.oplocku.length = new length4(length);
        ops.add(op);
        return this;
    }

    public CompoundBuilder withLayoutcommit(long offset, long length,
            boolean reclaim, stateid4 stateid, OptionalLong lastWriteOffset,
            layouttype4 layoutType, byte[] body) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTCOMMIT;
        op.oplayoutcommit = new LAYOUTCOMMIT4args();

        op.oplayoutcommit.loca_time_modify = new newtime4();
        op.oplayoutcommit.loca_time_modify.nt_timechanged = false;

        op.oplayoutcommit.loca_offset = new offset4(offset);
        op.oplayoutcommit.loca_length = new length4(length);
        op.oplayoutcommit.loca_reclaim = reclaim;
        op.oplayoutcommit.loca_stateid = stateid;
        op.oplayoutcommit.loca_last_write_offset = new newoffset4();
        op.oplayoutcommit.loca_last_write_offset.no_newoffset = lastWriteOffset.isPresent();
        if (lastWriteOffset.isPresent()) {
            op.oplayoutcommit.loca_last_write_offset.no_offset = new offset4(lastWriteOffset.getAsLong());
        }
        op.oplayoutcommit.loca_layoutupdate = new layoutupdate4();
        op.oplayoutcommit.loca_layoutupdate.lou_type = layoutType.getValue();
        op.oplayoutcommit.loca_layoutupdate.lou_body = body;

        ops.add(op);
        return this;
    }

    public CompoundBuilder withLayoutStats(long offset, long length, stateid4 stateid,
            io_info4 readInfo, io_info4 writeInfo, deviceid4 deviceid,
            layoutupdate4 layoutupdate) {

        nfs_argop4 op  = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTSTATS;
        op.oplayoutstats = new LAYOUTSTATS4args();

        op.oplayoutstats.lsa_offset = new offset4(offset);
        op.oplayoutstats.lsa_length = new length4(length);
        op.oplayoutstats.lsa_stateid = stateid;
        op.oplayoutstats.lsa_read = readInfo;
        op.oplayoutstats.lsa_write = writeInfo;
        op.oplayoutstats.lsa_deviceid = deviceid;
        op.oplayoutstats.lsa_layoutupdate = layoutupdate;

        ops.add(op);
        return this;
    }

    public CompoundBuilder withLayoutError(long offset, long length, stateid4 stateid,
            device_error4[] errors) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTERROR;
        op.oplayouterror = new LAYOUTERROR4args();

        op.oplayouterror.lea_offset = new offset4(offset);
        op.oplayouterror.lea_length = new length4(length);
        op.oplayouterror.lea_stateid = stateid;
        op.oplayouterror.lea_errors = errors;

        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetXattr(String name) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETXATTR;
        op.opgetxattr = new GETXATTR4args();
        op.opgetxattr.gxa_name = name;
        ops.add(op);

        return this;
    }

    public CompoundBuilder withSetXattr(String name, byte[] value, int mode) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_SETXATTR;
        op.opsetxattr = new SETXATTR4args();
        op.opsetxattr.sxa_name = name;
        op.opsetxattr.sxa_value = new xattrvalue4(value);
        op.opsetxattr.sxa_option = mode;
        ops.add(op);

        return this;
    }

    public CompoundBuilder withListXattrs(int cookie, int count) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LISTXATTRS;
        op.oplistxattrs = new LISTXATTRS4args();
        op.oplistxattrs.lxa_cookie = new nfs_cookie4(cookie);
        op.oplistxattrs.lxa_maxcount = new count4(count);
        ops.add(op);

        return this;
    }

    public CompoundBuilder withRemoveXattr(String name) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_REMOVEXATTR;
        op.opremovexattr = new REMOVEXATTR4args();
        op.opremovexattr.rxa_name = name;
        ops.add(op);

        return this;
    }

    public CompoundBuilder withIntraServerCopy(stateid4 srcStateid, stateid4 dstStateid, long srcOffset, long dstOffset, long length, boolean sync, boolean consecutive) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_COPY;
        op.opcopy = new COPY4args();
        op.opcopy.ca_consecutive = consecutive;
        op.opcopy.ca_synchronous = sync;
        op.opcopy.ca_source_server = new netloc4[]{};
        op.opcopy.ca_src_stateid = srcStateid;
        op.opcopy.ca_dst_stateid = dstStateid;
        op.opcopy.ca_src_offset = new offset4(srcOffset);
        op.opcopy.ca_dst_offset = new offset4(dstOffset);
        op.opcopy.ca_count = new length4(length);

        ops.add(op);

        return this;
    }


    public COMPOUND4args build() {
        final COMPOUND4args compound4args = new COMPOUND4args();
        compound4args.tag = new utf8str_cs(tag);
        compound4args.minorversion = new uint32_t(minorversion);

        compound4args.argarray = ops.toArray(new nfs_argop4[0]);

        return compound4args;
    }

    private static byte[] encodeAttrs(XdrAble ... attrs) {

        try (Xdr xdr = new Xdr(1024)) {

            xdr.beginEncoding();

            for (XdrAble attr: attrs) {
                attr.xdrEncode(xdr);
            }

            xdr.endEncoding();
            return xdr.getBytes();
        } catch (IOException never_happens) {
            // ignore
        }

        throw new RuntimeException("must never get here");
    }
}
