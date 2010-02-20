package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.CREATE_SESSION4resok;
import org.dcache.chimera.nfs.v4.xdr.CREATE_SESSION4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OperationCREATE_SESSION extends AbstractNFSv4Operation {


	private static final Logger _log = Logger.getLogger(OperationCREATE_SESSION.class.getName());

	public OperationCREATE_SESSION(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_CREATE_SESSION);
	}

	@Override
	public boolean process(CompoundContext context) {
    	CREATE_SESSION4res res = new CREATE_SESSION4res();


    	Long clientId = Long.valueOf(_args.opcreate_session.csa_clientid.value.value);
    	int seqId = _args.opcreate_session.csa_sequence.value.value;

    	try {

    		NFSv41Session session = null;

    		/*
    		 * check for correct arguments
    		 */
    		if( _args.opcreate_session.csa_fore_chan_attrs.ca_rdma_ird.length > 1 ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADXDR, "bad size of rdma_ird");
    		}

    		/*
    		 * check for correct flags
    		 */
    		if( ( _args.opcreate_session.csa_flags.value
    				& ~( nfs4_prot.CREATE_SESSION4_FLAG_PERSIST |nfs4_prot.CREATE_SESSION4_FLAG_CONN_RDMA |
    				nfs4_prot.CREATE_SESSION4_FLAG_CONN_BACK_CHAN)) != 0) {

                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "bad ceate_session flag");
    		}

    		NFS4Client client = NFSv4StateHandler.getInstace().getClientByID(clientId);

    		/*
    		 * Phase 1:
    		 *
             * Client record lookup. The server looks up the client ID in its client record table.
             * If the server contains no records with client ID equal to clientid_arg,
             * then most likely the client's state has been purged during a period of inactivity,
             * possibly due to a loss of connectivity. NFS4ERR_STALE_CLIENTID is returned,
             * and no changes are made to any client records on the server.
             * Otherwise, the server goes to phase 2.
             */
    		if(client == null ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_STALE_CLIENTID, "client not known");
    		}

    		/*
    		 * Phase 2:
    		 *
    		 * Sequence id processing. If csa_sequenceid is equal to the sequence id in the client ID's slot,
    		 * then this is a replay of the previous CREATE_SESSION request, and the server returns
    		 * the cached result. If csa_sequenceid is not equal to the sequence id in the slot,
    		 * and is more than one greater (accounting for wraparound),
    		 * then the server returns the error NFS4ERR_SEQ_MISORDERED, and does not change the slot.
    		 * If csa_sequenceid is equal to the slot's sequence id + 1 (accounting for wraparound),
    		 * then the slot's sequence id is set to csa_sequenceid, and the CREATE_SESSION processing
    		 * goes to the next phase. A subsequent new CREATE_SESSION call MUST use a csa_sequence
    		 * that is one greater than last successfully used.
    		 */

    		if( seqId == client.currentSeqID()  ) {
    			// retransmit
    	    	_log.log(Level.FINE, "CREATE_SESSION4 retransmit session: {0}", client.currentSeqID() );
    		}

    		if(client.currentSeqID() < _args.opcreate_session.csa_sequence.value.value | 0 > _args.opcreate_session.csa_sequence.value.value){
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_SEQ_MISORDERED, "bad sequence id: " + client.currentSeqID() + " / " + _args.opcreate_session.csa_sequence.value.value);
    		}

    		if( !client.principal().equals(Integer.toString(context.getUser().getUID())) && !client.isConfirmed() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_CLID_INUSE, "client already in use: " + client.principal()+ " " + context.getUser().getUID());
    		}

   			if(client.sessions().isEmpty() ) {
                            _log.log(Level.FINE, "set client {0} confirmed", client);
   			    client.confirmed(true);
   			}


    		//check of a contrived replay result
    		if( seqId != _args.opcreate_session.csa_sequence.value.value  ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_SEQ_MISORDERED, "bad sequence id: " + client.currentSeqID() + " / " + seqId);
    		}

    		if (client.currentSeqID() != 0)
    			session = client.getSession(client.currentSeqID()-1);

    		if(session == null){

	    		if( seqId > client.currentSeqID() +1 ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_SEQ_MISORDERED, "bad sequence id: " + client.currentSeqID() + " / " + seqId);
	    		}

                    session = new NFSv41Session(client,_args.opcreate_session.csa_fore_chan_attrs.ca_maxrequests.value.value );
	            client.addSession( session);
                    _log.log(Level.FINE, "adding new session [{0}]",  session.id() );
	            NFSv4StateHandler.getInstace().sessionById(session.id(), session);
	    		client.confirmed();
	    		client.nextSeqID();

	            client.inetAddress( context.getRpcCall().getTransport().getRemoteSocketAddress().getAddress() );

    		}

    		client.refreshLeaseTime();
    		//client.updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);

	    	res.csr_resok4 = new CREATE_SESSION4resok();

                res.csr_resok4.csr_sessionid = session.id();
	    	res.csr_resok4.csr_sequence = _args.opcreate_session.csa_sequence;

	    	/* we do not support callback connections on the same line*/
	    	res.csr_resok4.csr_flags = new uint32_t( _args.opcreate_session.csa_flags.value ^ nfs4_prot.CREATE_SESSION4_FLAG_CONN_BACK_CHAN);
	    	/*res.csr_resok4.csr_headerpadsize = _args.opcreate_session.csa_headerpadsize;

	    	res.csr_resok4.csr_conn_binding_opts = new conn_binding4res();
	    	res.csr_resok4.csr_conn_binding_opts.cbr_enforce = false;
	    	res.csr_resok4.csr_conn_binding_opts.cbr_hash_alg_info = new hash_alg_info4();*/

	    	res.csr_resok4.csr_fore_chan_attrs = _args.opcreate_session.csa_fore_chan_attrs;
	    	res.csr_resok4.csr_back_chan_attrs = _args.opcreate_session.csa_back_chan_attrs;

	    	if (client.isConfirmed())
                res.csr_resok4.csr_flags = new uint32_t(res.csr_resok4.csr_flags.value | nfs4_prot.EXCHGID4_FLAG_CONFIRMED_R);

	    	res.csr_status = nfsstat4.NFS4_OK;

        }catch(ChimeraNFSException ne) {
    		_log.log(Level.INFO, "CREATE_SESSION4res : {0}",  ne.getMessage());
    		res.csr_status = ne.getStatus();
    	}catch(Exception e) {
    		_log.log(Level.SEVERE, "CREATE_SESSION4 : {0}", e);
    		res.csr_status = nfsstat4.NFS4ERR_SERVERFAULT;
    	}

       _result.opcreate_session = res;

            context.processedOperations().add(_result);
            return res.csr_status == nfsstat4.NFS4_OK;
	}

}
