package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LOOKUP4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileNotFoundHimeraFsException;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;

public class OperationLOOKUP extends AbstractNFSv4Operation {


	private static final Logger _log = Logger.getLogger(OperationLOOKUP.class.getName());

	OperationLOOKUP(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_LOOKUP);
	}

	@Override
	public NFSv4OperationResult process() {
        LOOKUP4res res = new LOOKUP4res();

        try {

            String name = NameFilter.convert(_args.oplookup.objname.value.value.value);

            if( _fh.currentInode().isLink() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_SYMLINK, "parent not a symbolic link");
            }

        	if( !_fh.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "parent not a directory");
        	}

            if( name.length() > NFSv4Defaults.NFS4_MAXFILENAME ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NAMETOOLONG, "path too long");
            }

            if( name.equals(".") || name.equals("..") ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADNAME, "bad name '.' or '..'");
            }

            FsInode newInode = _fh.currentInode().inodeOf(name);
	        if( !newInode.exists() ) {
	          	res.status = nfsstat4.NFS4ERR_NOENT;
	         }

             _fh.currentInode( newInode );
	         res.status = nfsstat4.NFS4_OK;

        }catch(FileNotFoundHimeraFsException he) {
        	res.status = nfsstat4.NFS4ERR_NOENT;
        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.log(Level.SEVERE, "Error: ", e);
        	res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

       _result.oplookup = res;

        return new NFSv4OperationResult(_result, res.status);
	}

}
