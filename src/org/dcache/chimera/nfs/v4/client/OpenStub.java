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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.dcache.chimera.nfs.v4.xdr.OPEN4args;
import org.dcache.chimera.nfs.v4.xdr.attrlist4;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.clientid4;
import org.dcache.chimera.nfs.v4.xdr.component4;
import org.dcache.chimera.nfs.v4.xdr.createhow4;
import org.dcache.chimera.nfs.v4.xdr.createmode4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.fattr4_mode;
import org.dcache.chimera.nfs.v4.xdr.fattr4_size;
import org.dcache.chimera.nfs.v4.xdr.mode4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.open_claim4;
import org.dcache.chimera.nfs.v4.xdr.open_claim_type4;
import org.dcache.chimera.nfs.v4.xdr.open_owner4;
import org.dcache.chimera.nfs.v4.xdr.openflag4;
import org.dcache.chimera.nfs.v4.xdr.opentype4;
import org.dcache.chimera.nfs.v4.xdr.seqid4;
import org.dcache.chimera.nfs.v4.xdr.state_owner4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.utf8string;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrBuffer;
import org.dcache.xdr.XdrEncodingStream;

public class OpenStub {

    public static nfs_argop4 normalCREATE(String path, int sequenceId,
            clientid4 clientid, int access) {
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
        claim.file = new component4(new utf8str_cs(new utf8string(path
                .getBytes())));
        claim.delegate_type = nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        claim.file_delegate_prev = null;
        claim.oc_delegate_stateid = null;
        claim.delegate_type = 0;
        claim.delegate_cur_info = null;

        op.opopen.claim = claim;

        return op;
    }

    public static nfs_argop4 normalREAD(String path, int sequenceId,
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
        claim.file = new component4(new utf8str_cs(new utf8string(path
                .getBytes())));
        claim.delegate_type = nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
        claim.file_delegate_prev = null;
        claim.oc_delegate_stateid = null;
        claim.delegate_type = 0;
        claim.delegate_cur_info = null;

        op.opopen.claim = claim;

        return op;
    }

    private static bitmap4 openFattrBitmap() {

        List<Integer> attrs = new ArrayList<Integer>();

        attrs.add(nfs4_prot.FATTR4_SIZE);
        attrs.add(nfs4_prot.FATTR4_MODE);


        bitmap4 afttrBitmap = new bitmap4();
        afttrBitmap.value = new uint32_t[2];
        afttrBitmap.value[0] = new uint32_t();
        afttrBitmap.value[1] = new uint32_t();

        for( Integer mask : attrs) {
            int bit;
            uint32_t bitmap;
            if( mask > 31 ) {
                bit = mask - 32;
                bitmap = afttrBitmap.value[1];
            }else{
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
