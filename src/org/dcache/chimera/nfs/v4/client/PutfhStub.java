package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.PUTFH4args;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_fh4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;

public class PutfhStub {


    public static nfs_argop4 generateRequest( nfs_fh4 fh) {

        nfs_argop4 op = new nfs_argop4();
        op.opputfh = new PUTFH4args();
        op.opputfh.object = fh;

        op.argop = nfs_opnum4.OP_PUTFH;

        return op;

    }

}
