/*
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
package org.dcache.chimera.nfs.v4.client;

import com.google.common.base.Splitter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.dcache.chimera.nfs.v4.xdr.*;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrBuffer;
import org.dcache.xdr.XdrEncodingStream;

public class CompoundBuilder {

    private final utf8string NO_TAG = new utf8string(new byte[0]);
    private utf8string tag = NO_TAG;
    private int minorversion = 1;
    private List<nfs_argop4> ops = new ArrayList<nfs_argop4>();

    public CompoundBuilder withTag(String tag) {
        this.tag = new utf8string(tag);
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

            op.oplookup.objname = new component4(new utf8str_cs(s));
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

    public CompoundBuilder withReaddir(long cookie, verifier4 verifier) {

        nfs_argop4 op = new nfs_argop4();
        op.opreaddir = new READDIR4args();
        op.opreaddir.cookie = new nfs_cookie4(new uint64_t(cookie));
        op.opreaddir.dircount = new count4(new uint32_t(10000));
        op.opreaddir.maxcount = new count4(new uint32_t(10000));
        op.opreaddir.attr_request = new bitmap4(new uint32_t[]{new uint32_t(0), new uint32_t(0)});
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

        nfstime4 releaseDate = new nfstime4();
        releaseDate.nseconds = new uint32_t(0);
        releaseDate.seconds = new int64_t(System.currentTimeMillis() / 1000);

        op.opexchange_id.eia_client_impl_id[0].nii_date = releaseDate;
        op.opexchange_id.eia_clientowner = new client_owner4();
        op.opexchange_id.eia_clientowner.co_ownerid = co_ownerid.getBytes();

        op.opexchange_id.eia_clientowner.co_verifier = new verifier4();
        op.opexchange_id.eia_clientowner.co_verifier.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        byte[] locVerifier = Long.toHexString(releaseDate.seconds.value).getBytes();


        int len = Math.min(locVerifier.length, nfs4_prot.NFS4_VERIFIER_SIZE);
        System.arraycopy(locVerifier, 0, op.opexchange_id.eia_clientowner.co_verifier.value, 0, len);

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
        chan_attrs.ca_headerpadsize = new count4(new uint32_t(0));
        chan_attrs.ca_maxoperations = new count4(new uint32_t(8192));
        chan_attrs.ca_maxrequests = new count4(new uint32_t(8192));
        chan_attrs.ca_maxrequestsize = new count4(new uint32_t(8192));
        chan_attrs.ca_maxresponsesize = new count4(new uint32_t(8192));
        chan_attrs.ca_maxresponsesize_cached = new count4(new uint32_t(8));
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
            int slotid, int highSlotid, int sequence) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_SEQUENCE;
        op.opsequence = new SEQUENCE4args();
        op.opsequence.sa_cachethis = CacheThis;

        slotid4 sId = new slotid4();
        sId.value = new uint32_t(slotid);
        op.opsequence.sa_slotid = sId;

        slotid4 HsId = new slotid4();
        HsId.value = new uint32_t(highSlotid);
        op.opsequence.sa_highest_slotid = HsId;

        sequenceid4 seq = new sequenceid4();
        seq.value = new uint32_t(++sequence);
        op.opsequence.sa_sequenceid = seq;

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

        args.target = new component4();
        args.target.value = new utf8str_cs(path);

        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_REMOVE;
        op.opremove = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withWrite(long offset, byte[] data, stateid4 stateid) {
        WRITE4args args = new WRITE4args();

        args.stable = stable_how4.FILE_SYNC4;

        args.offset = new offset4(new uint64_t(offset));

        args.stateid = stateid;

        args.data = ByteBuffer.wrap(data);
        args.data.position(data.length);

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_WRITE;
        op.opwrite = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withRead(int count, long offset, stateid4 stateid) {
        READ4args args = new READ4args();
        args.count = new count4(new uint32_t(count));
        args.offset = new offset4(new uint64_t(offset));

        args.stateid = stateid;

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_READ;
        op.opread = args;
        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetdevicelist() {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICELIST;
        op.opgetdevicelist = new GETDEVICELIST4args();

        op.opgetdevicelist.gdla_cookie = new nfs_cookie4();
        op.opgetdevicelist.gdla_cookie.value = new uint64_t(0);

        op.opgetdevicelist.gdla_cookieverf = new verifier4();
        op.opgetdevicelist.gdla_cookieverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        op.opgetdevicelist.gdla_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;

        op.opgetdevicelist.gdla_maxdevices = new count4();
        op.opgetdevicelist.gdla_maxdevices.value = new uint32_t(256);
        ops.add(op);
        return this;
    }

    public CompoundBuilder withGetdeviceinfo(deviceid4 devId) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_GETDEVICEINFO;
        op.opgetdeviceinfo = new GETDEVICEINFO4args();

        op.opgetdeviceinfo.gdia_device_id = devId;
        op.opgetdeviceinfo.gdia_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;

        count4 cn = new count4();
        cn.value = new uint32_t(0xffffff);
        op.opgetdeviceinfo.gdia_maxcount = cn;
        op.opgetdeviceinfo.gdia_notify_types = new bitmap4();
        op.opgetdeviceinfo.gdia_notify_types.value = new uint32_t[1];
        op.opgetdeviceinfo.gdia_notify_types.value[0] = new uint32_t(0);

        ops.add(op);
        return this;
    }

    public CompoundBuilder withLayoutreturn(long offset, long len, byte[] body, stateid4 stateid) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTRETURN;
        op.oplayoutreturn = new LAYOUTRETURN4args();

        op.oplayoutreturn.lora_reclaim = false;
        op.oplayoutreturn.lora_iomode = layoutiomode4.LAYOUTIOMODE4_ANY;
        op.oplayoutreturn.lora_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
        op.oplayoutreturn.lora_layoutreturn = new layoutreturn4();
        op.oplayoutreturn.lora_layoutreturn.lr_returntype = layoutreturn_type4.LAYOUTRETURN4_FILE;
        op.oplayoutreturn.lora_layoutreturn.lr_layout = new layoutreturn_file4();
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_offset = new offset4(new uint64_t(offset));
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_length = new length4(new uint64_t(len));
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_body = body;
        op.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_stateid = stateid;

        ops.add(op);
        return this;
    }

    public CompoundBuilder withClose(stateid4 stateid) {
        CLOSE4args args = new CLOSE4args();

        args.seqid = new seqid4(new uint32_t(0));
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

        op.opopen.seqid = new seqid4(new uint32_t(sequenceId));

        state_owner4 owner = new state_owner4();
        owner.clientid = clientid;
        owner.owner = "JUnitChimera".getBytes();
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

        attr.attrmask = openFattrBitmap();
        attr.attr_vals = new attrlist4();
        attr.attr_vals.value = openAttrs();

        how.createattrs = attr;
        how.createverf = new verifier4(new byte[nfs4_prot.NFS4_VERIFIER_SIZE]);
        how.mode = createmode4.GUARDED4;

        flag.how = how;
        op.opopen.openhow = flag;

        open_claim4 claim = new open_claim4();
        claim.claim = open_claim_type4.CLAIM_NULL;
        claim.file = new component4(new utf8str_cs(path));
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

        args.objname = new component4(new utf8str_cs(path));
        args.objtype = new createtype4();
        args.objtype.type = nfs_ftype4.NF4DIR;
        args.createattrs = new fattr4();

        args.createattrs.attr_vals = new attrlist4();
        args.createattrs.attr_vals.value = openAttrs();

        args.createattrs.attrmask = openFattrBitmap();


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

        op.opopen.seqid = new seqid4(new uint32_t(sequenceId));

        // if ((access & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_DELEG_MASK) == 0){
        // access |= nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        // }
        // op.opopen.share_access = new uint32_t(access);
        op.opopen.share_access = new uint32_t(nfs4_prot.OPEN4_SHARE_ACCESS_READ);
        op.opopen.share_deny = new uint32_t(nfs4_prot.OPEN4_SHARE_DENY_NONE);

        state_owner4 owner = new state_owner4();
        owner.clientid = clientid;
        owner.owner = "JUnitChimera".getBytes();
        op.opopen.owner = new open_owner4(owner);

        openflag4 flag = new openflag4();
        flag.opentype = opentype4.OPEN4_NOCREATE;

        createhow4 how = new createhow4();
        how.mode = createmode4.UNCHECKED4;
        flag.how = how;
        op.opopen.openhow = flag;

        open_claim4 claim = new open_claim4();
        claim.claim = open_claim_type4.CLAIM_NULL;
        claim.file = new component4(new utf8str_cs(path));
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
            int layout_type, int iomode, int offset, int length, int minlength,
            int maxcount, stateid4 stateid) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTGET;
        op.oplayoutget = new LAYOUTGET4args();

        op.oplayoutget.loga_signal_layout_avail = signal_layout_avail;
        op.oplayoutget.loga_layout_type = layout_type;
        op.oplayoutget.loga_iomode = iomode;

        offset4 off = new offset4();
        uint64_t u64 = new uint64_t(offset);
        off.value = u64;

        length4 cnLong = new length4();
        cnLong.value = new uint64_t(length);

        length4 cnZero = new length4();
        cnZero.value = new uint64_t(minlength);

        count4 cnShort = new count4();
        cnShort.value = new uint32_t(maxcount);

        op.oplayoutget.loga_offset = off;
        op.oplayoutget.loga_length = cnLong;
        op.oplayoutget.loga_minlength = cnZero;
        op.oplayoutget.loga_maxcount = cnShort;

        op.oplayoutget.loga_stateid = stateid;
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

    private static bitmap4 openFattrBitmap() {

        List<Integer> attrs = new ArrayList<Integer>();

        attrs.add(nfs4_prot.FATTR4_SIZE);
        attrs.add(nfs4_prot.FATTR4_MODE);


        bitmap4 afttrBitmap = new bitmap4();
        afttrBitmap.value = new uint32_t[2];
        afttrBitmap.value[0] = new uint32_t();
        afttrBitmap.value[1] = new uint32_t();

        for (Integer mask : attrs) {
            int bit;
            uint32_t bitmap;
            if (mask > 31) {
                bit = mask - 32;
                bitmap = afttrBitmap.value[1];
            } else {
                bit = mask;
                bitmap = afttrBitmap.value[0];
            }

            bitmap.value |= 1 << bit;

        }

        return afttrBitmap;
    }

    private static byte[] openAttrs() {

        XdrEncodingStream xdr = new XdrBuffer(1024);
        try {
            xdr.beginEncoding();

            mode4 fmode = new mode4();
            fmode.value = new uint32_t(0755);
            fattr4_mode mode = new fattr4_mode(fmode);
            fattr4_size size = new fattr4_size(new uint64_t(0));

            size.xdrEncode(xdr);
            mode.xdrEncode(xdr);

        } catch (OncRpcException never_happens) {
            // ignore
        } catch (IOException never_happens) {
            // ignored
        }

        xdr.endEncoding();
        ByteBuffer b = xdr.body();
        byte[] retBytes = new byte[b.remaining()];
        b.get(retBytes);

        return retBytes;
    }
}
