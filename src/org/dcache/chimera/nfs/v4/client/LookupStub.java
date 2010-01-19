package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.LOOKUP4args;
import org.dcache.chimera.nfs.v4.xdr.component4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.utf8string;

public class LookupStub {

    public static nfs_argop4 generateRequest(String path) {
        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LOOKUP;
        op.oplookup = new LOOKUP4args();

        op.oplookup.objname = new component4(new utf8str_cs(new utf8string(path
                .getBytes())));

        return op;
    }
}
