package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.CREATE_SESSION4args;
import org.dcache.chimera.nfs.v4.xdr.callback_sec_parms4;
import org.dcache.chimera.nfs.v4.xdr.channel_attrs4;
import org.dcache.chimera.nfs.v4.xdr.clientid4;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.sequenceid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;

public class CreateSessionStub {

    public static nfs_argop4 standard(clientid4 eir_clientid,
            sequenceid4 eir_sequenceid) {
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

        op.opcreate_session.csa_clientid = eir_clientid;
        op.opcreate_session.csa_sequence = eir_sequenceid;
        op.opcreate_session.csa_flags = new uint32_t(0);
        op.opcreate_session.csa_fore_chan_attrs = chan_attrs;
        op.opcreate_session.csa_back_chan_attrs = chan_attrs;
        op.opcreate_session.csa_cb_program = new uint32_t(123);

        callback_sec_parms4[] cb = new callback_sec_parms4[1];
        callback_sec_parms4 callb = new callback_sec_parms4();
        callb.cb_secflavor = nfs4_prot.AUTH_NONE;
        cb[0] = callb;
        op.opcreate_session.csa_sec_parms = new callback_sec_parms4[1];
        op.opcreate_session.csa_sec_parms = cb;
        return op;
    }

}
