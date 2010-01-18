package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTFH4res;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.nfs.NFSHandle;

public class OperationPUTFH extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationPUTFH.class.getName());

	public OperationPUTFH(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_PUTFH);
	}

	@Override
	public boolean process(CompoundContext context) {
        PUTFH4res res = new PUTFH4res();

        try {
            context.currentInode( NFSHandle.toFsInode(context.getFs(), _args.opputfh.object.value) );
            _log.log(Level.FINEST, "NFS Request  PUTFH4 current: {0}", context.currentInode().toFullString());
            res.status = nfsstat4.NFS4_OK;
        }catch(IllegalArgumentException iae) {
        	res.status = nfsstat4.NFS4ERR_BADHANDLE;
        }catch(Exception e) {
            _log.log(Level.SEVERE, "PUTFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opputfh = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}

}
