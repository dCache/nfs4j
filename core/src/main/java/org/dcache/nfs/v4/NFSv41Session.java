/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadSlotException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.sessionid4;

import com.google.common.io.BaseEncoding;

public class NFSv41Session {

    private final sessionid4 _session;
    /**
     * Session reply slots.
     */
    private final SessionSlot[] _slots;
    private final NFS4Client _client;
    private final int _maxOps;
    private final int _maxCbOps;

    private final int _maxCbRequests;
    private final Set<SessionConnection> _boundConnections;

    /**
     * Create new session for the given client.
     *
     * @param client The client that owns this session.
     * @param sessionid The session id uniquely identifying session on the server.
     * @param maxRequests The maximum number of concurrent requests.
     * @param maxCbRequests The maximum number of concurrent callback requests.
     * @param maxOps The maximum number of compound operations per requests.
     * @param maxCbOps The maximum number of compound operations per callback requests.
     */
    public NFSv41Session(NFS4Client client, sessionid4 sessionid, int maxRequests, int maxCbRequests, int maxOps,
            int maxCbOps) {
        _client = client;
        _slots = new SessionSlot[maxRequests];
        _session = sessionid;
        _maxOps = maxOps;
        _maxCbOps = maxCbOps;
        _maxCbRequests = maxCbRequests;
        _boundConnections = new HashSet<>();
    }

    public sessionid4 id() {
        return _session;
    }

    public NFS4Client getClient() {
        return _client;
    }

    /**
     * Get maximum slot id.
     *
     * @return max slot id.
     */
    public int getHighestSlot() {
        return _slots.length - 1;
    }

    public int getCbHighestSlot() {
        return _maxCbRequests - 1;
    }

    /**
     * Get highest slot id used.
     *
     * @return slot id or -1 if there are no sloths have been used yet
     */
    public int getHighestUsedSlot() {
        int id;
        for (id = getHighestSlot(); id >= 0 && _slots[id] == null; id--) {
            /*
             * NOP. We only move pointer
             */
        }
        return id;
    }

    public List<nfs_resop4> checkCacheSlot(int slot, int sequence)
            throws ChimeraNFSException {
        return getSessionSlot(slot).acquire(sequence);
    }

    /**
     * Get cache slot for given id.
     *
     * @param slot
     * @return cache slot.
     * @throws ChimeraNFSException
     */
    public SessionSlot getSessionSlot(int slot) throws ChimeraNFSException {

        if (slot < 0 || slot > getHighestSlot()) {
            throw new BadSlotException("slot id overflow");
        }

        if (_slots[slot] == null) {
            _slots[slot] = new SessionSlot();
        }

        return _slots[slot];
    }

    /**
     * Get maximal number of operations server will accept for this session.
     *
     * @return number of operations
     */
    public int getMaxOps() {
        return _maxOps;
    }

    /**
     * Get maximal number of call-back operations client will accept for this session.
     *
     * @return number of operations
     */
    public int getMaxCbOps() {
        return _maxCbOps;
    }

    @Override
    public String toString() {
        return _client.getRemoteAddress() + " : " + BaseEncoding.base16().lowerCase().encode(_session.value);
    }

    /**
     * Binds the session to a given {@link SessionConnection} on if session has no bindings,
     *
     * @param connection to bind
     */
    public synchronized void bindIfNeeded(SessionConnection connection) {
        if (_boundConnections.isEmpty()) {
            bindToConnection(connection);
        }
    }

    /**
     * Binds the session to a given {@link SessionConnection}
     *
     * @param connection to bind
     */
    public synchronized void bindToConnection(SessionConnection connection) {
        _boundConnections.add(connection);
    }

    /**
     * Check if session can be destroyed by client on a given connection.
     *
     * @param connection
     * @return true, if session has no bindings or is bound to given connection.
     */
    public synchronized boolean isReleasableBy(SessionConnection connection) {
        return _boundConnections.isEmpty() || _boundConnections.contains(connection);
    }
}
