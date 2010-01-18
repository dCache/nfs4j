package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.PUTROOTFH4res;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;

public class OperationPUTROOTFH extends AbstractNFSv4Operation {


    private static final Logger _log = Logger.getLogger(OperationPUTROOTFH.class.getName());

	public OperationPUTROOTFH(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 op, ExportFile exports) {
		super(fs, exports, call$, fh, op, nfs_opnum4.OP_PUTROOTFH);
	}

	@Override
	public NFSv4OperationResult process() {

        PUTROOTFH4res res = new PUTROOTFH4res();

        try {
            _fh.currentInode( FsInode.getRoot(_fs));
        	res.status = nfsstat4.NFS4_OK;
        }catch(Exception e) {
            _log.log(Level.SEVERE, "PUTROOTFH4:", e);
        	res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opputrootfh = res;

        return new NFSv4OperationResult(_result, res.status);


	}

}
