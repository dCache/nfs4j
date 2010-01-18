package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.DESTROY_SESSION4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.apache.log4j.Logger;

public class OperationDESTROY_SESSION extends AbstractNFSv4Operation {


	private static final Logger _log = Logger.getLogger(OperationDESTROY_SESSION.class.getName());

	public OperationDESTROY_SESSION(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_DESTROY_SESSION);
	}

	@Override
	public boolean process(CompoundContext context) {

		DESTROY_SESSION4res  res = new DESTROY_SESSION4res();

		try {

    		NFSv41Session session = NFSv4StateHandler.getInstace().sessionById(_args.opdestroy_session.dsa_sessionid.value);
    		if(session == null) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADSESSION, "client not found");
    		}

    		NFS4Client client = session.getClient();
   			client.removeSession(session);

   			/*
   			 * remove client if there is not sessions any more
   			 */
   			if( client.sessions().isEmpty() ) {
   			    _log.debug("remove client: no sessions any more");
   			    NFSv4StateHandler.getInstace().removeClient(client);
   			}

			res.dsr_status = nfsstat4.NFS4_OK;

        }catch(ChimeraNFSException hne) {
			res.dsr_status = hne.getStatus();
		}catch(Exception e){
			_log.error("DESTROY_SESSION: ", e);
			res.dsr_status = nfsstat4.NFS4ERR_SERVERFAULT;
		}

		_result.opdestroy_session = res;
                context.processedOperations().add(_result);
                return res.dsr_status == nfsstat4.NFS4_OK;
    }

}
