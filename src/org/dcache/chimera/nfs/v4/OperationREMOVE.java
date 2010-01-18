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
import org.dcache.chimera.FsInode;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;

public class OperationREMOVE extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationREMOVE.class.getName());

    OperationREMOVE(nfs_argop4 args) {
	super(args, nfs_opnum4.OP_REMOVE);
    }

    @Override
    public boolean process(CompoundContext context) {

	REMOVE4res res = new REMOVE4res();

	try {

	    FsInode parentInode = context.currentInode();

        if (!context.currentInode().isDirectory()) {
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


	    Stat inodeStat = context.currentInode().inodeOf(name).statCache();
	    Stat parentStat = parentInode.statCache();

	    UnixAcl acl = new UnixAcl(inodeStat.getUid(), inodeStat.getGid(), inodeStat.getMode() & 0777);
//	    if (!_permissionHandler.isAllowed(acl, context.getUser(), AclHandler.ACL_DELETE)) {
//      throw new ChimeraNFSException(nfsstat4.NFS4ERR_ACCESS, "Permission denied.");
//	    }
	    acl = new UnixAcl(parentStat.getUid(), parentStat.getGid(), parentStat.getMode() & 0777);
	    if (!_permissionHandler.isAllowed(acl, context.getUser(), AclHandler.ACL_DELETE)) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_ACCESS, "Permission denied.");
	    }

	    boolean rc = context.currentInode().remove(name);
	    if (!rc && context.currentInode().isDirectory()) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTEMPTY, "directory not empty");
	    }

	    res.status = nfsstat4.NFS4_OK;
	    res.resok4 = new REMOVE4resok();
	    res.resok4.cinfo = new change_info4();
	    res.resok4.cinfo.atomic = true;
	    res.resok4.cinfo.before = new changeid4(new uint64_t(context.currentInode().statCache().getMTime()));
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

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;

    }
}
