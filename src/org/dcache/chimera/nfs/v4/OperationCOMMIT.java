package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.COMMIT4res;
import org.apache.log4j.Logger;

public class OperationCOMMIT extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationCOMMIT.class.getName());

	public OperationCOMMIT(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_COMMIT);
	}

    @Override
    public boolean process(CompoundContext context) {
        _result.opcommit = new COMMIT4res();
        _result.opcommit.status = nfsstat4.NFS4ERR_NOTSUPP;
        context.processedOperations().add(_result);
        return false;
    }

}
