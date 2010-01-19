package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.REMOVE4args;
import org.dcache.chimera.nfs.v4.xdr.component4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.utf8string;

public class RemoveStub {


    public static nfs_argop4 generateRequest(String path) {

        REMOVE4args args = new REMOVE4args();

        args.target = new component4();
        args.target.value = new utf8str_cs(new utf8string(path.getBytes()) );

        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_REMOVE;
        op.opremove = args;

        return op;

    }

}
