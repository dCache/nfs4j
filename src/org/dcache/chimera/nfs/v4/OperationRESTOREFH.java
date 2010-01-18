package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.RESTOREFH4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationRESTOREFH extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationRESTOREFH.class.getName());

	OperationRESTOREFH(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_RESTOREFH);
	}

	@Override
	public NFSv4OperationResult process() {

        RESTOREFH4res res = new RESTOREFH4res();

        try {
            _fh.restoreSavedInode();
            res.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException he) {
        	res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("RESTOREFH4:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.oprestorefh = res;

        return new NFSv4OperationResult(_result, res.status);
	}

}
