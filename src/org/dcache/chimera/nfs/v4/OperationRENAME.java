package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.RENAME4res;
import org.dcache.chimera.nfs.v4.xdr.RENAME4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.FileNotFoundHimeraFsException;
import org.dcache.chimera.FsInode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationRENAME extends AbstractNFSv4Operation {

        private static final Logger _log = LoggerFactory.getLogger(OperationRENAME.class);

	OperationRENAME(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_RENAME);
	}

	@Override
	public boolean process(CompoundContext context) {
    	RENAME4res res = new RENAME4res();

    	try {

    		FsInode sourceDir = context.savedInode();
    		FsInode destDir = context.currentInode();

            if( ! sourceDir.isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "source path not a directory");
            }

            if( ! destDir.isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "destination path  not a directory");
            }

            String oldName = NameFilter.convert (_args.oprename.oldname.value.value.value);
            String newName = NameFilter.convert (_args.oprename.newname.value.value.value);

            if( oldName.length() == 0 ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "zero-length name");
            }

            if( newName.length() == 0 ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "zero-length name");
            }


            if( oldName.equals(".") || oldName.equals("..") ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADNAME, "bad name '.' or '..'");
            }

            if( newName.equals(".") || newName.equals("..") ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADNAME, "bad name '.' or '..'");
            }


            if( sourceDir.fsId() != destDir.fsId() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_XDEV, "cross filesystem request");
            }

            _log.debug("Rename: src={} name={} dest={} name={}", new Object[] {
                    sourceDir,
                    oldName,
                    destDir,
                    newName
                });

            context.getFs().move(sourceDir, oldName, destDir, newName);

            res.resok4 = new RENAME4resok();

            res.resok4.source_cinfo = new change_info4();
            res.resok4.source_cinfo.atomic = true;
            res.resok4.source_cinfo.before = new changeid4( new uint64_t(sourceDir.statCache().getMTime()));
            res.resok4.source_cinfo.after = new changeid4( new uint64_t( System.currentTimeMillis()) );

            res.resok4.target_cinfo = new change_info4();
            res.resok4.target_cinfo.atomic = true;
            res.resok4.target_cinfo.before = new changeid4( new uint64_t(sourceDir.statCache().getMTime()));
            res.resok4.target_cinfo.after = new changeid4( new uint64_t( System.currentTimeMillis()) );


            res.status = nfsstat4.NFS4_OK;

    	}catch(FileNotFoundHimeraFsException fnf) {
    		res.status = nfsstat4.NFS4ERR_NOENT;
    	}catch(ChimeraFsException hfe) {
    		res.status = nfsstat4.NFS4ERR_SERVERFAULT;
                _log.error("RENAME: {}", hfe.getMessage());
        }catch(ChimeraNFSException he) {
            _log.error("RENAME: {}", he.getMessage());
            res.status = he.getStatus();
        }


       _result.oprename = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}

}
