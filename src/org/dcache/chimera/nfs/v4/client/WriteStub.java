package org.dcache.chimera.nfs.v4.client;

import java.nio.ByteBuffer;
import org.dcache.chimera.nfs.v4.xdr.WRITE4args;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.offset4;
import org.dcache.chimera.nfs.v4.xdr.stable_how4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;

public class WriteStub {

    public  static nfs_argop4 generateRequest(long offset, byte[] data, stateid4 stateid ) {

        WRITE4args args = new WRITE4args();

        args.stable = stable_how4.FILE_SYNC4;

        args.offset = new offset4(new uint64_t(offset));

        args.stateid = stateid;

        args.data = ByteBuffer.wrap(data);

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_WRITE;
        op.opwrite = args;

        return op;
    }

}
