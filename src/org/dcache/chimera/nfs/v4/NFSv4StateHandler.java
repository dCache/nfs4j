package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dcache.chimera.nfs.v4.xdr.sessionid4;
import org.dcache.utils.Cache;

public class NFSv4StateHandler {

    private static final Logger _log = Logger.getLogger(NFSv4StateHandler.class.getName());

    private static final NFSv4StateHandler HANDLER = new NFSv4StateHandler();


    private List<NFS4Client> _clients = new ArrayList<NFS4Client>();

    // all seen by server
    private static Map<String, NFS4Client> _clientsByVerifier = new HashMap<String, NFS4Client>();


    // mapping between server generated clietid and nfs_client_id, not confirmed yet
    private static Map<Long, NFS4Client> _clientsByServerId = new HashMap<Long, NFS4Client>();
    private static Map<String, Long> _clientsByStateId = new HashMap<String, Long>();

    private final Cache<sessionid4, NFSv41Session> _sessionById =
            new Cache<sessionid4, NFSv41Session>("NFSv41 sessions", 5000, Long.MAX_VALUE, TimeUnit.SECONDS.toMillis(NFSv4Defaults.NFS4_LEASE_TIME*2));

    private static Map<String, NFS4Client> _clientByOwner = new HashMap<String, NFS4Client>();

    private NFSv4StateHandler() {}


    public static NFSv4StateHandler getInstace() {
        return HANDLER;
    }

    public void removeClient(NFS4Client client) {

        for(NFSv41Session session: client.sessions() ) {
            _sessionById.remove( session.id() );
        }
        _clientsByServerId.remove(client.id_srv());
        _clientsByServerId.remove(client.id_srv());
        _clientByOwner.remove(client.id());
        _clientsByVerifier.remove( new String( client.verifier() ) ) ;
        _clients.remove(client);

    }

    public void addClient(NFS4Client newClient) {
        _clients.add(newClient);
        _clientsByServerId.put(newClient.id_srv(), newClient);
        _clientsByVerifier.put(new String(newClient.verifier()), newClient);
        _clientByOwner.put( newClient.id(), newClient);
    }

    public NFS4Client getClientByID( Long id) {
        return _clientsByServerId.get(id);
    }


    public Long getClientIdByStateId(String stateId) {
        return _clientsByStateId.get(stateId);
    }


    public void addClientByID(Long id, NFS4Client client) {
        _clientsByServerId.put(id, client);
    }


    public void addClinetByStateID(String stateId, Long clientId) {
        _clientsByStateId.put(stateId, clientId);
    }


    public void addClientByVerifier( byte[] verifier, NFS4Client client) {
        _clientsByVerifier.put(new String(verifier), client );
    }

    public NFS4Client getClientByVerifier(byte[] client) {
        return _clientsByVerifier.get(new String(client));
    }


    public NFSv41Session sessionById( sessionid4 id) {
       return _sessionById.get(id);
    }

    public void sessionById( sessionid4 id, NFSv41Session session) {
        _sessionById.put(id, session);
    }

    public NFS4Client clientByOwner( String ownerid) {
        return _clientByOwner.get(ownerid);
    }

    public void updateClientLeaseTime(stateid4  stateid) throws ChimeraNFSException {

        String stateID = new String(stateid.other);

        Long clientId = _clientsByStateId.get(stateID);
        if(clientId == null ) {
            throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "No client ID associated with sate."  );
        }

        NFS4Client client = _clientsByServerId.get(clientId);
        if(client == null ) {
            throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "No client associated with client id."  );
        }

        NFS4State state = client.state(stateID);
        if( state == null) {
            throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "State not know to the client."  );
        }

        if( !state.isConfimed() ) {
            throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "State is not confirmed"  );
        }

        if( state.seqid() != stateid.seqid.value ) {
            throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "Sequence id miss match."  );
        }


        client.updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);

    }

    public int acquire_state(stateid4  stateid, boolean allow) throws ChimeraNFSException{

        byte[] array_zero = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] array_allOnes ={0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf};

        if ( Arrays.equals(stateid.other, array_zero)){
            if (stateid.seqid.value != 0){
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "bad seqid.");
            }
            if (allow == true){
                return 0;
            }
            else{
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "Can't use 0 as stateid");
            }
        }
        else if (Arrays.equals(stateid.other, array_allOnes)){
            if(stateid.seqid.value == 0xffffffff){
                if (allow == true){
                    return 1;
                }else{
                    throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "Can't use 1 as stateid");
                }
            }else if (stateid.seqid.value == 0){
                //Use Current stateid
                return stateid.seqid.value;
            }else{
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "bad seqid");
            }
        }
        if (stateid.seqid.value != 0){
            throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "bad seqid");
        }
        return stateid.seqid.value;
    }

    public List<NFS4Client> getClients() {
        return new CopyOnWriteArrayList<NFS4Client>(_clients);
    }
}
