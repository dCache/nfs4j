package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.clientid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.SETCLIENTID4resok;
import org.dcache.chimera.nfs.v4.xdr.SETCLIENTID4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationSETCLIENTID extends AbstractNFSv4Operation {


	private static final Logger _log = LoggerFactory.getLogger(OperationSETCLIENTID.class);

	OperationSETCLIENTID(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_SETCLIENTID);
	}

	@Override
	public boolean process(CompoundContext context) {

		 SETCLIENTID4res res = new SETCLIENTID4res();

	        try {


	            byte[] clientid = _args.opsetclientid.client.id; // clientid

	            if( context.getStateHandler().getClientByVerifier(clientid) != null ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_CLID_INUSE, "Client Id In use");
	            }

		        String r_addr = _args.opsetclientid.callback.cb_location.na_r_addr;
		        String r_netid = _args.opsetclientid.callback.cb_location.na_r_netid;
		        int program = _args.opsetclientid.callback.cb_program.value;

                        NFS4Client client = new NFS4Client(context.getRpcCall().getTransport().getRemoteSocketAddress(),
                                context.getRpcCall().getTransport().getLocalSocketAddress(),
                            new String(_args.opsetclientid.client.id),_args.opsetclientid.client.verifier.value, null );

		        try {
	    	        ClientCB cb = new ClientCB(r_addr, r_netid, program);
	    	        //	TODO: work around. client should send correct IP
	    	        cb = new ClientCB(  HimeraNFS4Utils.inetAddress2rAddr(context.getRpcCall().getTransport().getRemoteSocketAddress() ), r_netid, program);
                    _log.debug("Client callback: {}", cb);
	                client.setCB(cb);
		        }catch(Exception ignode_call_back) {
                    _log.debug("no callback defined for: {}", context.getRpcCall().getTransport().getRemoteSocketAddress().getAddress());
		        }

		        context.getStateHandler().addClient(client);

		        res.resok4 = new SETCLIENTID4resok();
		        res.resok4.clientid = new clientid4();
		        res.resok4.clientid.value = new uint64_t(client.id_srv());
		        res.resok4.setclientid_confirm = new verifier4();
		        res.resok4.setclientid_confirm.value = client.verifier();
		        res.status = nfsstat4.NFS4_OK;


        }catch(ChimeraNFSException he) {
            _log.debug("SETCLIENTID: ", he.getMessage() );
	        res.status = he.getStatus();
	    }catch(Exception e) {
            _log.error("SETCLIENTID: " , e);
	        res.status = nfsstat4.NFS4ERR_SERVERFAULT;
	    }

        _result.opsetclientid = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;

	}

}
