package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.SAVEFH4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.apache.log4j.Logger;

public class OperationSAVEFH extends AbstractNFSv4Operation {


	private static final Logger _log = Logger.getLogger(OperationSAVEFH.class.getName());

	OperationSAVEFH(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_SAVEFH);
	}

	@Override
	public boolean process(CompoundContext context) {
        SAVEFH4res res = new SAVEFH4res();

        try {
            context.saveCurrentInode();
            res.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException he) {
        	res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("SAVEFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opsavefh = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;

	}

}
