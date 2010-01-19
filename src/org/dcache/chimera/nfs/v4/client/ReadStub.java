package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.READ4args;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.offset4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;

public class ReadStub {

    /**
     *
     * @param count
     * @param offset
     * @param stateid
     * @return
     */
    public static nfs_argop4 generateRequest(int count,  long offset, stateid4 stateid) {

        READ4args args = new READ4args();
        args.count = new count4(new uint32_t(count));
        args.offset = new offset4(new uint64_t(offset));

        args.stateid = stateid;

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_READ;
        op.opread = args;

        return op;

    }

}
