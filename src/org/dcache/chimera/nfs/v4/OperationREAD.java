package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.READ4resok;
import org.dcache.chimera.nfs.v4.xdr.READ4res;
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

public class OperationREAD extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationREAD.class.getName());

	public OperationREAD(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_READ);
	}

	@Override
	public NFSv4OperationResult process() {
        READ4res res = new READ4res();


        try {

            if( _fh.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_ISDIR, "path is a directory");
            }

            if( _fh.currentInode().isLink() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "path is a symlink");
            }

            Stat inodeStat = _fh.currentInode().statCache();

            UnixAcl fileAcl = new UnixAcl(inodeStat.getUid(), inodeStat.getGid(),inodeStat.getMode() & 0777 );
            if ( ! _permissionHandler.isAllowed(fileAcl, _user, AclHandler.ACL_READ)  ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_ACCESS, "Permission denied."  );
            }


            NFSv4StateHandler.getInstace().updateClientLeaseTime(_args.opread.stateid);


            long offset = _args.opread.offset.value.value;
            int count = _args.opread.count.value.value;

            byte[] buf = new byte[count];

            int bytesReaded = _fh.currentInode().read(offset, buf, 0, count);
            if( bytesReaded < 0 ) {
                throw new IOHimeraFsException("IO not allowd");
            }

            res.status = nfsstat4.NFS4_OK;
            res.resok4 = new READ4resok();

            if (bytesReaded != count) {
                res.resok4.data = new byte[bytesReaded];
                System.arraycopy(buf, 0, res.resok4.data, 0, bytesReaded);
            } else {
                res.resok4.data = buf;
            }

            if( offset + bytesReaded >= inodeStat.getSize() ) {
                res.resok4.eof = true;
            }

        }catch(IOHimeraFsException hioe) {
        	if(_log.isDebugEnabled() ) {
        		_log.debug("READ: " + hioe.getMessage() );
        	}
            res.status = nfsstat4.NFS4ERR_IO;
        }catch(ChimeraNFSException he) {
        	if(_log.isDebugEnabled() ) {
        		_log.debug("READ: " + he.getMessage() );
        	}
            res.status = he.getStatus();
        }catch(ChimeraFsException hfe) {
            res.status = nfsstat4.NFS4ERR_NOFILEHANDLE;
        }


       _result.opread = res;

        return new NFSv4OperationResult(_result, res.status);
	}

}
