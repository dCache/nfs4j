package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOOKUPP4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;

public class OperationLOOKUPP extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLOOKUPP.class.getName());

	OperationLOOKUPP(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_LOOKUPP);
	}

	@Override
	public NFSv4OperationResult process() {
        LOOKUPP4res res = new LOOKUPP4res();

        try {

        	if( !_fh.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "parent not a directory");
        	}

            FsInode parent = _fh.currentInode().getParent();
            if( (parent == null) || _fh.currentInode().toString().equals(FsInode.getRoot(_fs).toString()) ) {
                res.status = nfsstat4.NFS4ERR_NOENT;
            }else{
                _fh.currentInode( parent );
                res.status = nfsstat4.NFS4_OK;
            }

        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.log(Level.SEVERE, "Error: ", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.oplookupp = res;

        return new NFSv4OperationResult(_result, res.status);
	}

}
