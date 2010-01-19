package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;

public class SavefhStub {

    public static nfs_argop4 generateRequest() {


        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_SAVEFH;

        return op;

    }

}
