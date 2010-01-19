package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.CLOSE4args;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.seqid4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;

public class CloseStub {

    public static nfs_argop4 generateRequest(stateid4 stateid) {

        CLOSE4args args = new CLOSE4args();

        args.seqid = new seqid4(new uint32_t (0));
        args.open_stateid = stateid;


        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_CLOSE;
        op.opclose = args;

        return op;

    }


}
