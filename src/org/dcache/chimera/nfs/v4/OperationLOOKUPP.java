package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOOKUPP4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.FsInode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLOOKUPP extends AbstractNFSv4Operation {

        private static final Logger _log = LoggerFactory.getLogger(OperationLOOKUPP.class);

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
            _log.debug("LOOKUPP: {}", he.getMessage());
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("Error: ", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.oplookupp = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}

}
