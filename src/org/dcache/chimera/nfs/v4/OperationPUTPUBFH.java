package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTPUBFH4res;
import java.util.logging.Logger;

public class OperationPUTPUBFH extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationPUTPUBFH.class.getName());

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
