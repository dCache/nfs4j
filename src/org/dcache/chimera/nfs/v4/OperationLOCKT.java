package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOCKT4res;
import org.apache.log4j.Logger;

public class OperationLOCKT extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLOCKT.class.getName());

	OperationLOCKT(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_LOCKT);
	}

    @Override
    public boolean process(CompoundContext context) {
        _result.oplockt = new LOCKT4res();
        _result.oplockt.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }

}
