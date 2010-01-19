package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.READDIR4args;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_cookie4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.verifier4;

public class ReaddirStub {


    public static nfs_argop4 generateRequest( long cookie, verifier4 verifier) {


        nfs_argop4 op = new nfs_argop4();
        op.opreaddir = new READDIR4args();
        op.opreaddir.cookie = new nfs_cookie4( new uint64_t(cookie) );
        op.opreaddir.dircount = new count4(new uint32_t(10000));
        op.opreaddir.maxcount = new count4(new uint32_t(10000));
        op.opreaddir.attr_request = new bitmap4( new uint32_t[] { new uint32_t(0), new uint32_t(0) });
        op.opreaddir.cookieverf =  verifier;

        op.argop = nfs_opnum4.OP_READDIR;

        return op;

    }


}
