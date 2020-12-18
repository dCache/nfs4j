/*
 * Copyright (c) 2017 Deutsches Elektronen-Synchroton,
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
import java.io.Serializable;
import org.dcache.nfs.status.BadSeqidException;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owner associated with the open/lock operations.
 */
public class StateOwner implements Serializable {

    private final static Logger _log = LoggerFactory.getLogger(StateOwner.class);

    private static final long serialVersionUID = -4712959403595550903L;

    /**
     * Per owner sequence to serialize opens with nfsv4.0
     */
    private int seq;

    /**
     * Client unique state owner.
     */
    private final state_owner4 owner;

    public StateOwner(state_owner4 owner, int seq) {
        this.owner = owner;
        this.seq = seq;
    }

    public synchronized void acceptAsNextSequence(seqid4 openSeqid) throws BadSeqidException {

        int next = seq + 1;
        if (next != openSeqid.value) {
            _log.error("Expected next sequence id {} but received {}",
                    next, openSeqid);
            seq = openSeqid.value;
        }
        else {
            seq = next;
        }
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StateOwner other = (StateOwner) obj;
        return this.owner.equals(other.owner);
    }

    public state_owner4 getRawStateOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass().getSimpleName())
                .add("owner", owner)
                .add("seq", seq)
                .toString();
    }
}
