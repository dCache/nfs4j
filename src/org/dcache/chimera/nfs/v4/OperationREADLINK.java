package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.linktext4;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.utf8string;
import org.dcache.chimera.nfs.v4.xdr.READLINK4res;
import org.dcache.chimera.nfs.v4.xdr.READLINK4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationREADLINK extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationREADLINK.class.getName());

	OperationREADLINK(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_READLINK);
	}

	@Override
	public NFSv4OperationResult process() {
        READLINK4res res = new READLINK4res();

        try {

        	if( !_fh.currentInode().isLink()  ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "not a symlink");
        	}

            byte[] link = _fh.currentInode().readlink();
            _log.debug("NFS Request  READLINK4 link: " + new String(link) );
            res.resok4 = new READLINK4resok();
            res.resok4.link = new linktext4();
            res.resok4.link.value = new utf8str_cs();
            res.resok4.link.value.value = new utf8string();
            res.resok4.link.value.value.value = link;
            res.status = nfsstat4.NFS4_OK;

        }catch(ChimeraNFSException he){
        	if(_log.isDebugEnabled() ) {
        		_log.debug("READLINK: " + he.getMessage() );
        	}
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("READLINK4", e);
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

      _result.opreadlink = res;

        return new NFSv4OperationResult(_result, res.status);
	}

}
