package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_fh4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.GETFH4res;
import org.dcache.chimera.nfs.v4.xdr.GETFH4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OperationGETFH extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationGETFH.class.getName());

	OperationGETFH(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_GETFH);
	}

	@Override
	public boolean process(CompoundContext context) {

        GETFH4res res = new GETFH4res();

        try {

	        res.resok4 = new GETFH4resok();
	        res.resok4.object = new nfs_fh4();
	        res.resok4.object.value = context.currentInode().toFullString().getBytes();
	        res.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException he) {
        	res.status = he.getStatus();
        }catch(Exception e) {
            _log.log(Level.SEVERE, "GETFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opgetfh = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;

	}

}
