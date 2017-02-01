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

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v4.xdr.stateid4;

public class NFS4State {

    private static final Logger LOG = LoggerFactory.getLogger(NFS4State.class);

    /*
        struct stateid4 {
            uint32_t        seqid;
            opaque          other[12];
        };

       This structure is used for the various state sharing mechanisms
        between the client and server.  For the client, this data structure
        is read-only.  The starting value of the seqid field is undefined.
        The server is required to increment the seqid field monotonically at
        each transition of the stateid.  This is important since the client
        will inspect the seqid in OPEN stateids to determine the order of
        OPEN processing done by the server.

     */

    private final stateid4 _stateid;
    private final StateOwner _owner;
    private boolean _isConfimed = false;
    private boolean _disposed = false;

    /**
     * A state (lock, layout)) can be derived from an open state.
     * If null, then this is the original open state.
     */
    private final NFS4State _openState;

    private final List<StateDisposeListener> _disposeListeners;

    public NFS4State(StateOwner owner, stateid4 stateid) {
        this(null, owner, stateid);
    }

    public NFS4State(NFS4State openState, StateOwner owner, stateid4 stateid) {
        _openState = openState;
        _owner = owner;
        _stateid = stateid;
        _disposeListeners = new ArrayList<>();
    }

    public void bumpSeqid() { ++ _stateid.seqid.value; }

    public stateid4 stateid() {
        return _stateid;
    }

    public void confirm() {
    	_isConfimed = true;
    }

    public boolean isConfimed() {
    	return _isConfimed;
    }

    /**
     * Release resources used by this State if not released yet.
     * Any subsequent call will have no effect.
     * @throws ChimeraNFSException on errors.
     */
    synchronized public final void tryDispose() throws ChimeraNFSException {
        if (!_disposed) {
            Iterator<StateDisposeListener> i = _disposeListeners.iterator();
            while(i.hasNext()) {
                StateDisposeListener listener = i.next();
                listener.notifyDisposed(this);
                i.remove();
            }
            dispose();
            _disposed = true;
        }
    }

    /**
     * Release resources used by this State if not released yet. Any subsequent
     * call will have no effect.
     */
    synchronized public final void disposeIgnoreFailures() {
        if (!_disposed) {
            Iterator<StateDisposeListener> i = _disposeListeners.iterator();
            while (i.hasNext()) {
                StateDisposeListener listener = i.next();
                try {
                    listener.notifyDisposed(this);
                } catch (ChimeraNFSException e) {
                    LOG.info("failed to notify: {}", e.getMessage());
                }
                i.remove();
            }
            dispose();
            _disposed = true;
        }
    }

    public NFS4State getOpenState() {
        return _openState == null? this : _openState;
    }

    /**
     * Release resources used by this State.
     */
    protected void dispose() {
        // NOP
    }

    public StateOwner getStateOwner() {
        return _owner;
    }

    synchronized public void addDisposeListener(StateDisposeListener disposeListener) {
        _disposeListeners.add(disposeListener);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass().getSimpleName())
                .add("stateid", _stateid)
                .add("open-stateid", _openState == null? null : _openState.stateid())
                .add("owner", _owner)
                .add("confirmed", _isConfimed)
                .omitNullValues()
                .toString();
    }
}
