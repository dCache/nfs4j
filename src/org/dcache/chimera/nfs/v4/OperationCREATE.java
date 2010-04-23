package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_ftype4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.CREATE4res;
import org.dcache.chimera.nfs.v4.xdr.CREATE4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationCREATE extends AbstractNFSv4Operation {


        private static final Logger _log = LoggerFactory.getLogger(OperationCREATE.class);


	OperationCREATE(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_CREATE);
	}

	@Override
	public boolean process(CompoundContext context) {

    	CREATE4res res = new CREATE4res();


    	fattr4 objAttr = _args.opcreate.createattrs;
    	int type = _args.opcreate.objtype.type;
    	String name = new String(_args.opcreate.objname.value.value.value);
    	FsInode inode = null;



    	try {

    		Stat parentStat = context.currentInode().statCache();


    		UnixAcl fileAcl = new UnixAcl(parentStat.getUid(), parentStat.getGid(),parentStat.getMode() & 0777 );


            if ( ! context.getAclHandler().isAllowed(fileAcl, context.getUser(), AclHandler.ACL_INSERT)  ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_ACCESS, "Permission denied."  );
            }

            if(name.length() == 0) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "bad path name");
            }

            if(name.length() > NFSv4Defaults.NFS4_MAXFILENAME ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NAMETOOLONG, "name too long");
            }

    		if(!context.currentInode().isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "not a directory");
    		}


            if( name.equals(".") || name.equals("..") ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADNAME, "bad name '.' or '..'");
            }

    		// TODO: this check have to be moved into JdbcFs
    		try {
    			inode = context.currentInode().inodeOf(name);
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_EXIST, "path already exist");
    		}catch(ChimeraFsException hfe) {	}

    		switch(type) {

    			case nfs_ftype4.NF4DIR:
	    			inode = context.currentInode().mkdir(name);
	    			break;
    			case nfs_ftype4.NF4LNK:
    				byte[] linkDest = _args.opcreate.objtype.linkdata.value.value.value;
    				inode = context.getFs().createLink(context.currentInode(), name, context.getUser().getUID(), context.getUser().getGID(), 777, linkDest);
    				break;

    			// we do not support other file types
    			case nfs_ftype4.NF4BLK:
    			case nfs_ftype4.NF4CHR:
    			case nfs_ftype4.NF4FIFO:
    			case nfs_ftype4.NF4SOCK:
    			case nfs_ftype4.NF4ATTRDIR:
    			case nfs_ftype4.NF4NAMEDATTR:
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTSUPP, "create of this type not supported");
    				// regular files handled by OPEN
    			case nfs_ftype4.NF4REG:
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADTYPE, "create of regular files handled by OPEN");
    			default:
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADTYPE, "bad file type");
    		}

			inode.setGID(context.getUser().getGID());
			inode.setUID(context.getUser().getUID());

    		res.status = nfsstat4.NFS4_OK;
    		res.resok4 = new CREATE4resok();
    		res.resok4.attrset = OperationSETATTR.setAttributes(objAttr, inode);
            res.resok4.cinfo = new change_info4();
            res.resok4.cinfo.atomic = true;
            res.resok4.cinfo.before = new changeid4( new uint64_t(context.currentInode().statCache().getMTime()));
            res.resok4.cinfo.after = new changeid4( new uint64_t( System.currentTimeMillis()) );

    		context.currentInode(inode);

        }catch(ChimeraNFSException he) {
            _log.debug("CREATE: {}", he.getMessage() );
    		res.status = he.getStatus();
    	}catch(Exception e) {
    		_log.error("CREATE: ", e);
    		res.status = nfsstat4.NFS4ERR_SERVERFAULT;
    	}

       _result.opcreate = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;

	}

}

