/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadSessionException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.StaleClientidException;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.sessionid4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.utils.Bytes;
import org.dcache.utils.Cache;
import org.dcache.utils.CacheElement;
import org.dcache.utils.NopCacheEventListener;

import static com.google.common.base.Preconditions.checkState;

public class NFSv4StateHandler {

    private static final Logger _log = LoggerFactory.getLogger(NFSv4StateHandler.class);

    /**
     * initial value of new state's sequence number.
     */
    private final static int STATE_INITIAL_SEQUENCE = 1;

    /**
     * client id generator.
     */
    private final AtomicInteger _clientId = new AtomicInteger(0);

    // mapping between server generated clietid and nfs_client_id, not confirmed yet
    private final Cache<clientid4, NFS4Client> _clientsByServerId;

    /**
     * Client's lease expiration time in milliseconds.
     */
    private final long _leaseTime;

    private boolean _running;

    /**
     * a system wide unique id of this state handler.
     */
    private final int _instanceId;

    private final FileTracker _openFileTracker = new FileTracker();

    public NFSv4StateHandler() {
        this(NFSv4Defaults.NFS4_LEASE_TIME, 0);
    }

    NFSv4StateHandler(long leaseTime, int instanceId) {
        _leaseTime = TimeUnit.SECONDS.toMillis(leaseTime);
        _clientsByServerId = new Cache<>("NFSv41 clients", 5000, Long.MAX_VALUE,
                _leaseTime * 2,
                new DeadClientCollector(),
                _leaseTime * 4, TimeUnit.MILLISECONDS);

        _running = true;
        _instanceId = instanceId;
    }

    public void removeClient(NFS4Client client) {

	synchronized (this) {
	    checkState(_running, "NFS state handler not running");
	    _clientsByServerId.remove(client.getId());
	}
        client.tryDispose();
    }

    private synchronized void addClient(NFS4Client newClient) {

        checkState(_running, "NFS state handler not running");
        _clientsByServerId.put(newClient.getId(), newClient);
    }

    public synchronized NFS4Client getClientByID(clientid4 clientid) throws ChimeraNFSException {

        checkState(_running, "NFS state handler not running");

        NFS4Client client = _clientsByServerId.get(clientid);
        if(client == null) {
            throw new StaleClientidException("bad client id.");
        }
        return client;
    }

    public synchronized NFS4Client getClientIdByStateId(stateid4 stateId) throws ChimeraNFSException {

        checkState(_running, "NFS state handler not running");

        clientid4 clientId = new clientid4(Bytes.getLong(stateId.other, 0));
        NFS4Client client = _clientsByServerId.get(clientId);
        if (client == null) {
            throw new BadStateidException("no client for stateid: " + stateId);
        }
        return client;
    }

    public synchronized NFS4Client getClient(sessionid4 id) throws ChimeraNFSException {
        checkState(_running, "NFS state handler not running");
        clientid4 clientId = new clientid4(Bytes.getLong(id.value, 0));
        NFS4Client client = _clientsByServerId.get(clientId);
        if (client == null) {
            throw new BadSessionException("session not found: " + id);
        }
        return client;
    }

    public synchronized NFS4Client clientByOwner(byte[] ownerid) {
        return _clientsByServerId.entries()
                .stream()
                .map(CacheElement::getObject)
                .filter(c -> c.isOwner(ownerid))
                .findAny()
                .orElse(null);
    }

    public void updateClientLeaseTime(stateid4  stateid) throws ChimeraNFSException {

        checkState(_running, "NFS state handler not running");
        NFS4Client client = getClientIdByStateId(stateid);
        NFS4State state = client.state(stateid);

        if( !state.isConfimed() ) {
            throw new BadStateidException("State is not confirmed"  );
        }

        Stateids.checkStateId(state.stateid(), stateid);
        client.updateLeaseTime();
    }

    public synchronized List<NFS4Client> getClients() {
        checkState(_running, "NFS state handler not running");
        return _clientsByServerId.entries().stream()
                .map(CacheElement::getObject)
                .collect(Collectors.toList());
    }

    public NFS4Client createClient(InetSocketAddress clientAddress, InetSocketAddress localAddress, int minorVersion,
            byte[] ownerID, verifier4 verifier, Principal principal, boolean callbackNeeded) {
        NFS4Client client = new NFS4Client(this, nextClientId(), minorVersion, clientAddress, localAddress, ownerID, verifier, principal, _leaseTime, callbackNeeded);
        addClient(client);
        return client;
    }

    /**
     * Get open files tacker.
     * @return open files tracker
     */
    public FileTracker getFileTracker() {
        return _openFileTracker;
    }

    private class DeadClientCollector extends NopCacheEventListener<clientid4, NFS4Client> {

        @Override
        public void notifyExpired(Cache<clientid4, NFS4Client> cache, NFS4Client client) {
            _log.info("Removing expired client: {}", client);
            client.tryDispose();
        }
    }

    /**
     * Check is the GRACE period expired.
     * @return true, if grace period expired.
     */
    public boolean hasGracePeriodExpired() {
        checkState(_running, "NFS state handler not running");
	/*
	 * As we do not have a persistent storage for state information,
	 * grace period makes no sense as it ends up as a simple delay
	 * before first IO request can be processed.
	 */
	return true;
    }

    private synchronized void drainClients() {
        _clientsByServerId.entries().stream()
                .map(CacheElement::getObject)
                .forEach(c -> {
                    c.tryDispose();
                    _clientsByServerId.remove(c.getId());
                });
    }

    /**
     * Shutdown session lease time watchdog thread.
     */
    public synchronized void shutdown() {
        checkState(_running, "NFS state handler not running");
        _running = false;
        drainClients();
        _clientsByServerId.shutdown();
    }

    /**
     * Get system wide unique id to identify this state handler.
     * @return system wide unique id
     */
    public int getInstanceId() {
        return _instanceId;
    }

    /**
     * Get system wide unique state handler id which have issued provided stateid.
     * @param stateid issuer of which have to be discovered
     * @return state hander id.
     */
    public static int getInstanceId(stateid4 stateid) {
        long clientid = Bytes.getLong(stateid.other, 0);
        return (int)(clientid >> 16) & 0xFFFF;
    }
    /*
     * Generate next client id. A composite number consist of timestamp, counter,
     * instance id:
     *
     * 0..........................31|32.......... 47|48.......63|
     * |-     timestamp            -| - instance id -|-  counter -|
     *
     * This schema allows us to have 2^16 unique client per second and
     * 2^16 instances of state handler.
     */
    private clientid4 nextClientId() {
        long now = (System.currentTimeMillis() / 1000);
        return new clientid4((now << 32) | (_instanceId << 16) | (_clientId.incrementAndGet() & 0x0000FFFF));
    }

    /**
     * Generate new state id associated with a given {@code client}.
     *
     * we construct 'other' fileld of state IDs as following:
     * |0 -  7| : client id
     * |8 - 11| : clients state counter
     *
     * @param client nfs client for which state is generated.
     * @param count the count of already generated state ids for give client.
     * @return new state id.
     */
    public stateid4 createStateId(NFS4Client client, int count) {
        byte[] other = new byte[12];
        Bytes.putLong(other, 0, client.getId().value);
        Bytes.putInt(other, 8, count);
        return new stateid4(other, STATE_INITIAL_SEQUENCE);
    }

    /**
     * Create new session identifier for a given {@code client}.
     *
     * we create session identifier as:
     * |0 -  7| : client id
     * |8 - 11| : reserved
     * |12 -15| : sequence id
     *
     * @param client nfs client for which new session id is generated.
     * @param sequence the count of already generated sessions for given client.
     * @return new session id.
     */
    public sessionid4 createSessionId(NFS4Client client, int sequence) {
        byte[] id = new byte[nfs4_prot.NFS4_SESSIONID_SIZE];
        Bytes.putLong(id, 0, client.getId().value);
        Bytes.putInt(id, 12, sequence);
        return new sessionid4(id);
    }

}
