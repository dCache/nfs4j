/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.nfs.v4;

import java.net.InetSocketAddress;
import java.security.Principal;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.ChimeraNFSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.dcache.nfs.v4.xdr.sessionid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.utils.Cache;
import org.dcache.utils.Bytes;
import org.dcache.utils.NopCacheEventListener;
import org.dcache.utils.Opaque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NFSv4StateHandler {

    private static final Logger _log = LoggerFactory.getLogger(NFSv4StateHandler.class);

    // all seen by server
    private final Map<verifier4, NFS4Client> _clientsByVerifier = new HashMap<>();


    // mapping between server generated clietid and nfs_client_id, not confirmed yet
    private final Map<Long, NFS4Client> _clientsByServerId = new HashMap<>();

    private final Cache<sessionid4, NFSv41Session> _sessionById =
            new Cache<>("NFSv41 sessions", 5000, Long.MAX_VALUE,
            TimeUnit.SECONDS.toMillis(NFSv4Defaults.NFS4_LEASE_TIME*2),
            new DeadSessionCollector(),
            NFSv4Defaults.NFS4_LEASE_TIME*4, TimeUnit.SECONDS);

    private final Map<Opaque, NFS4Client> _clientByOwner = new HashMap<>();

    /**
     * Client's lease expiration time in milliseconds.
     */
    private final long _leaseTime;

    /**
     * Server start time.
     */
    private final long _bootTime;

    public NFSv4StateHandler() {
        this(NFSv4Defaults.NFS4_LEASE_TIME*1000);
    }

    NFSv4StateHandler(long leaseTime) {
        _leaseTime = leaseTime;
	_bootTime = System.currentTimeMillis();
    }

    public synchronized void removeClient(NFS4Client client) {

        for(NFSv41Session session: client.sessions() ) {
            _sessionById.remove( session.id() );
        }

        _clientsByServerId.remove(client.getId());
        _clientByOwner.remove(client.getOwner());
        _clientsByVerifier.remove(client.verifier()) ;
        client.tryDispose();
    }

    private synchronized void addClient(NFS4Client newClient) {
        _clientsByServerId.put(newClient.getId(), newClient);
        _clientsByVerifier.put(newClient.verifier(), newClient);
        _clientByOwner.put( newClient.getOwner(), newClient);
    }

    public synchronized NFS4Client getClientByID( Long id) throws ChimeraNFSException {
        NFS4Client client = _clientsByServerId.get(id);
        if(client == null) {
            throw new ChimeraNFSException(nfsstat.NFSERR_STALE_CLIENTID, "bad client id.");
        }
        return client;
    }

    public NFS4Client getClientIdByStateId(stateid4 stateId) throws ChimeraNFSException {
        return getClientByID(Long.valueOf(Bytes.getLong(stateId.other, 0)));
    }

    public synchronized NFS4Client getClientByVerifier(verifier4 verifier) {
        return _clientsByVerifier.get(verifier);
    }


    public synchronized NFSv41Session sessionById( sessionid4 id) {
       return _sessionById.get(id);
    }

    public synchronized NFSv41Session removeSessionById(sessionid4 id) throws ChimeraNFSException {
        NFSv41Session session = _sessionById.remove(id);
        if (session == null) {
            throw new ChimeraNFSException(nfsstat.NFSERR_BADSESSION, "session not found");
        }

        detachSession(session);
        return session;
    }

    public synchronized void sessionById( sessionid4 id, NFSv41Session session) {
        _sessionById.put(id, session);
    }

    public synchronized NFS4Client clientByOwner( byte[] ownerid) {
        return _clientByOwner.get(new Opaque(ownerid));
    }

    public void updateClientLeaseTime(stateid4  stateid) throws ChimeraNFSException {

        NFS4Client client = getClientIdByStateId(stateid);
        NFS4State state = client.state(stateid);

        if( !state.isConfimed() ) {
            throw new ChimeraNFSException( nfsstat.NFSERR_BAD_STATEID, "State is not confirmed"  );
        }

        client.updateLeaseTime();
    }

    public synchronized List<NFS4Client> getClients() {
        return new ArrayList<>(_clientsByServerId.values());
    }

    public NFS4Client createClient(InetSocketAddress clientAddress, InetSocketAddress localAddress,
            byte[] ownerID, verifier4 verifier, Principal principal) {
        NFS4Client client = new NFS4Client(clientAddress, localAddress, ownerID, verifier, principal, _leaseTime);
        addClient(client);
        return client;
    }

    /**
     * Detach session from the client. Removes client, if there are no sessions
     * associated with client any more.
     *
     * @param session to detach.
     */
    private void detachSession(NFSv41Session session) {
        NFS4Client client = session.getClient();
        client.removeSession(session);

        /*
        * remove client if there is not sessions any more
        */
        if (!client.hasSessions()) {
            removeClient(client);
        }
    }

    private class DeadSessionCollector extends NopCacheEventListener<sessionid4, NFSv41Session> {

        @Override
        public void notifyExpired(Cache<sessionid4, NFSv41Session> cache, NFSv41Session session) {
            _log.info("Removing expired session: {}", session);
            detachSession(session);
        }
    }

    /**
     * Check is the GRACE period expired.
     * @return true, if grace period expired.
     */
    public boolean hasGracePeriodExpired() {
	return _bootTime + TimeUnit.SECONDS.toMillis(NFSv4Defaults.NFS4_LEASE_TIME) < System.currentTimeMillis();
    }
}
