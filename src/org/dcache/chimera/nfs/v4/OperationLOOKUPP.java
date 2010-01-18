package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOOKUPP4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.FsInode;

public class OperationLOOKUPP extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLOOKUPP.class.getName());

	OperationLOOKUPP(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_LOOKUPP);
	}

	@Override
	public boolean process(CompoundContext context) {
        LOOKUPP4res res = new LOOKUPP4res();

        try {

        	if( !context.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "parent not a directory");
        	}

            FsInode parent = context.currentInode().getParent();
            if( (parent == null) || context.currentInode().toString().equals(FsInode.getRoot(context.getFs()).toString()) ) {
                res.status = nfsstat4.NFS4ERR_NOENT;
            }else{
                context.currentInode( parent );
                res.status = nfsstat4.NFS4_OK;
            }

        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.log(Level.SEVERE, "Error: ", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.oplookupp = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}

}
