package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.DESTROY_SESSION4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationDESTROY_SESSION extends AbstractNFSv4Operation {


	private static final Logger _log = Logger.getLogger(OperationDESTROY_SESSION.class.getName());

	public OperationDESTROY_SESSION(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_DESTROY_SESSION);
	}

	@Override
	public NFSv4OperationResult process() {

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
    	return  new NFSv4OperationResult(_result, res.dsr_status);
    }

}
