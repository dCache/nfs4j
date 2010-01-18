package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LINK4resok;
import org.dcache.chimera.nfs.v4.xdr.LINK4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;

public class OperationLINK extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLINK.class.getName());

	OperationLINK(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_LINK);
	}

	@Override
	public NFSv4OperationResult process() {

		_result.oplink = new LINK4res();


		String newName = new String(_args.oplink.newname.value.value.value);

		try {

            Stat parentStat = _fh.currentInode().statCache();
            UnixAcl acl = new UnixAcl(parentStat.getUid(), parentStat.getGid(),parentStat.getMode() & 0777 );
            if ( ! _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_INSERT ) ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_ACCESS, "Permission denied."  );
            }

			_fs.createHLink(_fh.currentInode(),  _fh.savedInode(),newName );

			_result.oplink.resok4 = new LINK4resok();
			_result.oplink.resok4.cinfo = new change_info4();
			_result.oplink.resok4.cinfo.atomic = true;
			_result.oplink.resok4.cinfo.before = new changeid4( new uint64_t( _fh.savedInode().statCache().getMTime()));
			_result.oplink.resok4.cinfo.after = new changeid4( new uint64_t( System.currentTimeMillis()) );

			_result.oplink.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException hne){
			_result.oplink.status = hne.getStatus();
		}catch(Exception e) {
			_log.error("LINK: ", e);
		    _result.oplink.status = nfsstat4.NFS4ERR_RESOURCE;
		}

    	return  new NFSv4OperationResult(_result, _result.oplink.status);
	}

}
