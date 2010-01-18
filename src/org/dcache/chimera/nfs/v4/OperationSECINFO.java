package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.rpcsec_gss_info;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.secinfo4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.SECINFO4resok;
import org.dcache.chimera.nfs.v4.xdr.SECINFO4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationSECINFO extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationSECINFO.class.getName());

	OperationSECINFO(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_SECINFO);
	}

	@Override
	public NFSv4OperationResult process() {

        SECINFO4res res = new SECINFO4res();

        try {

            if( !_fh.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "not a directory");
            }

            res.resok4 = new SECINFO4resok();
            res.resok4.value = new secinfo4[1];

            res.resok4.value[0] = new secinfo4();
            res.resok4.value[0].flavor = RpcAuthType.UNIX;
            res.resok4.value[0].flavor_info = new rpcsec_gss_info();

            res.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }

        _result.opsecinfo = res;

        return new NFSv4OperationResult(_result, res.status);

	}

}
