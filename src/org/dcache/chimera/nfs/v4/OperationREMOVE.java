package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.REMOVE4resok;
import org.dcache.chimera.nfs.v4.xdr.REMOVE4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.FileNotFoundHimeraFsException;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;
import org.dcache.xdr.RpcCall;

public class OperationREMOVE extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationREMOVE.class.getName());

    OperationREMOVE(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
	super(fs, exports, call$, fh, args, nfs_opnum4.OP_REMOVE);
    }

    @Override
    public NFSv4OperationResult process() {

	REMOVE4res res = new REMOVE4res();

	try {

	    FsInode parentInode = _fh.currentInode();

        if (!_fh.currentInode().isDirectory()) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "parent not a directory");
        }

        String name = NameFilter.convert(_args.opremove.target.value.value.value);

        if (name.length() > NFSv4Defaults.NFS4_MAXFILENAME) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NAMETOOLONG, "name too long");
        }

        if (name.length() == 0) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "zero-length name");
        }

        if (name.equals(".") || name.equals("..")) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADNAME, "bad name '.' or '..'");
        }

        _log.log(Level.FINEST, "REMOVE: {0} : {1}",
                new Object[] {parentInode.toString(), name}
        );


	    Stat inodeStat = _fh.currentInode().inodeOf(name).statCache();
	    Stat parentStat = parentInode.statCache();

	    UnixAcl acl = new UnixAcl(inodeStat.getUid(), inodeStat.getGid(), inodeStat.getMode() & 0777);
//	    if (!_permissionHandler.isAllowed(acl, _user, AclHandler.ACL_DELETE)) {
//      throw new ChimeraNFSException(nfsstat4.NFS4ERR_ACCESS, "Permission denied.");
//	    }
	    acl = new UnixAcl(parentStat.getUid(), parentStat.getGid(), parentStat.getMode() & 0777);
	    if (!_permissionHandler.isAllowed(acl, _user, AclHandler.ACL_DELETE)) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_ACCESS, "Permission denied.");
	    }

	    boolean rc = _fh.currentInode().remove(name);
	    if (!rc && _fh.currentInode().isDirectory()) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTEMPTY, "directory not empty");
	    }

	    res.status = nfsstat4.NFS4_OK;
	    res.resok4 = new REMOVE4resok();
	    res.resok4.cinfo = new change_info4();
	    res.resok4.cinfo.atomic = true;
	    res.resok4.cinfo.before = new changeid4(new uint64_t(_fh.currentInode().statCache().getMTime()));
	    res.resok4.cinfo.after = new changeid4(new uint64_t(System.currentTimeMillis()));
	}catch(FileNotFoundHimeraFsException e){
	    res.status = nfsstat4.NFS4ERR_NOENT;
    } catch (ChimeraNFSException he) {
        _log.log(Level.FINEST, "REMOVE: ", he.getMessage());
	    res.status = he.getStatus();
	} catch (Exception e) {
        _log.log(Level.SEVERE, "REMOVE: ", e);
	    res.status = nfsstat4.NFS4ERR_SERVERFAULT;
	}

	_result.opremove = res;

	return new NFSv4OperationResult(_result, res.status);

    }
}
