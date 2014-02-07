/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.v4.xdr.CREATE_SESSION4args;
import org.dcache.nfs.v4.xdr.callback_sec_parms4;
import org.dcache.nfs.v4.xdr.channel_attrs4;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.sequenceid4;
import org.dcache.nfs.v4.xdr.uint32_t;

public class CreateSessionStub {

    public static nfs_argop4 standard(clientid4 eir_clientid,
            sequenceid4 eir_sequenceid) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_CREATE_SESSION;
        op.opcreate_session = new CREATE_SESSION4args();

        channel_attrs4 chan_attrs = new channel_attrs4();
        chan_attrs.ca_headerpadsize = new count4(0);
        chan_attrs.ca_maxoperations = new count4(128);
        chan_attrs.ca_maxrequests = new count4(8192);
        chan_attrs.ca_maxrequestsize = new count4(8192);
        chan_attrs.ca_maxresponsesize = new count4(8192);
        chan_attrs.ca_maxresponsesize_cached = new count4(8);
        chan_attrs.ca_rdma_ird = new uint32_t[0];

        op.opcreate_session.csa_clientid = eir_clientid;
        op.opcreate_session.csa_sequence = eir_sequenceid;
        op.opcreate_session.csa_flags = new uint32_t(nfs4_prot.CREATE_SESSION4_FLAG_PERSIST);
        op.opcreate_session.csa_fore_chan_attrs = chan_attrs;
        op.opcreate_session.csa_back_chan_attrs = chan_attrs;
        op.opcreate_session.csa_cb_program = new uint32_t(123);

        callback_sec_parms4[] cb = new callback_sec_parms4[1];
        callback_sec_parms4 callb = new callback_sec_parms4();
        callb.cb_secflavor = nfs4_prot.AUTH_NONE;
        cb[0] = callb;
        op.opcreate_session.csa_sec_parms = cb;
        return op;
    }

}
