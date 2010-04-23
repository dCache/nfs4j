package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTPUBFH4res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationPUTPUBFH extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationPUTPUBFH.class);

    OperationPUTPUBFH(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_PUTPUBFH);
    }

    @Override
    public boolean process(CompoundContext context) {
        _result.opputpubfh = new PUTPUBFH4res();
        _result.opputpubfh.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }
}
