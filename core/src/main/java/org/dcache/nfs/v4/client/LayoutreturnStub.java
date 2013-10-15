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
package org.dcache.nfs.v4.client;

import org.dcache.nfs.v4.xdr.LAYOUTRETURN4args;
import org.dcache.nfs.v4.xdr.layoutiomode4;
import org.dcache.nfs.v4.xdr.layoutreturn4;
import org.dcache.nfs.v4.xdr.layoutreturn_file4;
import org.dcache.nfs.v4.xdr.layoutreturn_type4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint64_t;

public class LayoutreturnStub {

    private LayoutreturnStub() {
    }

    public static nfs_argop4 generateRequest(long offset, long len, byte[] body, stateid4 stateid) {

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

        return op;
    }
}
