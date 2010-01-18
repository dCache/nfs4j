package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.RESTOREFH4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.apache.log4j.Logger;

public class OperationRESTOREFH extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationRESTOREFH.class.getName());

	OperationRESTOREFH(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_RESTOREFH);
	}

	@Override
	public boolean process(CompoundContext context) {

        RESTOREFH4res res = new RESTOREFH4res();

        try {
            context.restoreSavedInode();
            res.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException he) {
        	res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("RESTOREFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.oprestorefh = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}

}
