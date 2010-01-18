package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.dcache.chimera.nfs.v4.xdr.stable_how4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.WRITE4resok;
import org.dcache.chimera.nfs.v4.xdr.WRITE4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.IOHimeraFsException;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;

public class OperationWRITE extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationWRITE.class.getName());

	public OperationWRITE(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_WRITE);
	}

	@Override
	public NFSv4OperationResult process() {

		WRITE4res res = new WRITE4res();

    	try {


			if (_args.opwrite.offset.value.value + _args.opwrite.data.length > 0x3ffffffe){
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "Arbitrary value");
			 }


            if( _fh.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_ISDIR, "path is a directory");
    		}

            if( _fh.currentInode().isLink() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "path is a symlink");
            }

    		Stat inodeStat = _fh.currentInode().statCache();

            UnixAcl fileAcl = new UnixAcl(inodeStat.getUid(), inodeStat.getGid(),inodeStat.getMode() & 0777 );
            if ( ! _permissionHandler.isAllowed(fileAcl, _user, AclHandler.ACL_WRITE)  ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_ACCESS, "Permission denied."  );
            }


            if( _fh.getSession() == null ) {
                NFSv4StateHandler.getInstace().updateClientLeaseTime(_args.opwrite.stateid);
            }else{
                _fh.getSession().getClient().updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);
            }

	    	long offset = _args.opwrite.offset.value.value;
	    	int count = _args.opwrite.data.length;
	        int bytesWritten = _fh.currentInode().write(offset, _args.opwrite.data, 0, count);

	        if( bytesWritten < 0 ) {
	            throw new IOHimeraFsException("IO not allowed");
	        }

	        res.status = nfsstat4.NFS4_OK;
	        res.resok4 = new WRITE4resok();
	        res.resok4.count = new count4( new uint32_t(bytesWritten) );
	        res.resok4.committed = stable_how4.FILE_SYNC4;
	        res.resok4.writeverf = new verifier4();
	        res.resok4.writeverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

    	}catch(IOHimeraFsException hioe) {
    		if(_log.isDebugEnabled() ) {
    			_log.debug("WRITE: " + hioe.getMessage() );
    		}
    		res.status = nfsstat4.NFS4ERR_IO;
        }catch(ChimeraNFSException he) {
    		_log.debug("WRITE: " + he.getMessage() );
    		res.status = he.getStatus();
    	}catch(ChimeraFsException hfe) {
    		res.status = nfsstat4.NFS4ERR_NOFILEHANDLE;
    	}

       _result.opwrite = res;

        return new NFSv4OperationResult(_result, res.status);

	}

}
