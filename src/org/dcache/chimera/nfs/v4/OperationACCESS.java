package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.ACCESS4res;
import org.dcache.chimera.nfs.v4.xdr.ACCESS4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.JdbcFs;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;

public class OperationACCESS extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationACCESS.class.getName());

	OperationACCESS(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_ACCESS);
	}

	@Override
	public NFSv4OperationResult process() {

        ACCESS4res res = new ACCESS4res();

        if(_log.isDebugEnabled() ) {
        	_log.debug("NFS Request ACCESS uid: " + _user );
        }

        try {
            int reqAccess = _args.opaccess.access.value;
            Stat objStat = _fh.currentInode().statCache();
            UnixAcl acl = new UnixAcl(objStat.getUid(), objStat.getGid(),objStat.getMode() & 0777 );

            int realAccess = 0;


            if( (reqAccess & nfs4_prot.ACCESS4_EXECUTE) == nfs4_prot.ACCESS4_EXECUTE ) {
                if (  _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_EXECUTE ) ) {
                    realAccess |= nfs4_prot.ACCESS4_EXECUTE;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_EXTEND) == nfs4_prot.ACCESS4_EXTEND ) {
                if (  _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_INSERT ) ) {
                    realAccess |= nfs4_prot.ACCESS4_EXTEND;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_LOOKUP) == nfs4_prot.ACCESS4_LOOKUP ) {
                if (  _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_LOOKUP ) ) {
                    realAccess |= nfs4_prot.ACCESS4_LOOKUP;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_DELETE) == nfs4_prot.ACCESS4_DELETE ) {
                if (  _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_DELETE ) ) {
                    realAccess |= nfs4_prot.ACCESS4_DELETE;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_MODIFY) == nfs4_prot.ACCESS4_MODIFY ) {
                if (  _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_WRITE ) ){
                    realAccess |= nfs4_prot.ACCESS4_MODIFY;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_READ) == nfs4_prot.ACCESS4_READ ) {
                if (  _permissionHandler.isAllowed(acl, _user, AclHandler.ACL_READ ) ) {
                    realAccess |= nfs4_prot.ACCESS4_READ;
                }
            }

            res.resok4 = new ACCESS4resok();
            res.resok4.access = new uint32_t( realAccess );
            res.resok4.supported = new uint32_t( realAccess );

            res.status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException he) {
        	if(_log.isDebugEnabled() ) {
        		_log.debug("ACCESS: " + he.getMessage() );
        	}
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("ACCESS:", e);
            res.status = nfsstat4.NFS4ERR_RESOURCE;
        }

        _result.opaccess = res;

        return new NFSv4OperationResult(_result, res.status);

	}

}
