package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.ACCESS4res;
import org.dcache.chimera.nfs.v4.xdr.ACCESS4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.apache.log4j.Logger;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;

public class OperationACCESS extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationACCESS.class.getName());

	OperationACCESS(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_ACCESS);
	}

	@Override
	public boolean process(CompoundContext context) {

        ACCESS4res res = new ACCESS4res();

        if(_log.isDebugEnabled() ) {
        	_log.debug("NFS Request ACCESS uid: " + context.getUser() );
        }

        try {
            int reqAccess = _args.opaccess.access.value;
            Stat objStat = context.currentInode().statCache();
            UnixAcl acl = new UnixAcl(objStat.getUid(), objStat.getGid(),objStat.getMode() & 0777 );

            int realAccess = 0;


            if( (reqAccess & nfs4_prot.ACCESS4_EXECUTE) == nfs4_prot.ACCESS4_EXECUTE ) {
                if (  context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_EXECUTE ) ) {
                    realAccess |= nfs4_prot.ACCESS4_EXECUTE;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_EXTEND) == nfs4_prot.ACCESS4_EXTEND ) {
                if (  context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_INSERT ) ) {
                    realAccess |= nfs4_prot.ACCESS4_EXTEND;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_LOOKUP) == nfs4_prot.ACCESS4_LOOKUP ) {
                if (  context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_LOOKUP ) ) {
                    realAccess |= nfs4_prot.ACCESS4_LOOKUP;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_DELETE) == nfs4_prot.ACCESS4_DELETE ) {
                if (  context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_DELETE ) ) {
                    realAccess |= nfs4_prot.ACCESS4_DELETE;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_MODIFY) == nfs4_prot.ACCESS4_MODIFY ) {
                if (  context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_WRITE ) ){
                    realAccess |= nfs4_prot.ACCESS4_MODIFY;
                }
            }

            if( (reqAccess & nfs4_prot.ACCESS4_READ) == nfs4_prot.ACCESS4_READ ) {
                if (  context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_READ ) ) {
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

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;

	}

}
