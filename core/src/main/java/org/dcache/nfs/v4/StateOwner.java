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

import java.io.Serializable;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadSeqidException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.state_owner4;

import com.google.common.base.MoreObjects;

/**
 * Owner associated with the open/lock operations.
 */
public class StateOwner implements Serializable {

    private static final long serialVersionUID = -4712959403595550903L;

    /**
     * Per owner sequence to serialize opens with nfsv4.0
     */
    private int seq;

    /**
     * Cached response of the last consumed sequence id to answer retransmissions, see rfc7530#section-9.1.7.
     */
    private transient nfs_resop4 lastResponse;

    /**
     * Cached failure of the last consumed sequence id to answer retransmissions.
     */
    private transient ChimeraNFSException lastFailure;

    /**
     * Client unique state owner.
     */
    private final state_owner4 owner;

    public StateOwner(state_owner4 owner, int seq) {
        this.owner = owner;
        this.seq = seq;
    }

    /**
     * Create a state owner for nfsv4.0 whose first sequenced request is expected to carry the given sequence id,
     * typically the request creating the owner. The sequence id is consumed by the operation once it completed,
     * see {@link #consumeSequence} and rfc7530#section-9.1.7.
     *
     * @param owner client unique state owner.
     * @param seqid sequence id expected as the next one.
     * @return the state owner.
     */
    public static StateOwner expectingSequence(state_owner4 owner, seqid4 seqid) {
        // initialize the last consumed sequence id such that the given one is the expected next
        return new StateOwner(owner, seqid.value - 1);
    }

    public synchronized void acceptAsNextSequence(seqid4 openSeqid) throws BadSeqidException {

        int next = seq + 1;
        if (next != openSeqid.value) {
            throw new BadSeqidException();
        }
        seq = next;
    }

    /**
     * Validate the sequence id of a sequenced nfsv4.0 request, see rfc7530#section-9.1.7. A request repeating the
     * last consumed sequence id is a retransmission and must be answered with the cached response of the original
     * request, see {@link #replayLastResponse()}. A request with the next sequence id must be executed and its
     * outcome recorded with {@link #consumeSequence} or {@link #consumeSequenceOnFailure}. Any other sequence id is
     * out of order.
     *
     * @param seqid sequence id of the request.
     * @return true if the request is a retransmission of the last consumed request.
     * @throws BadSeqidException if the sequence id is out of order.
     */
    public synchronized boolean isSequenceReplay(seqid4 seqid) throws BadSeqidException {
        if (seqid.value == seq) {
            return true;
        }
        if (seqid.value == seq + 1) {
            return false;
        }
        throw new BadSeqidException();
    }

    /**
     * Get the cached response of the last consumed sequence id to answer a retransmission detected by
     * {@link #isSequenceReplay}. If the original request failed, the recorded failure is thrown again.
     *
     * @return the response of the original request.
     * @throws ChimeraNFSException recorded failure of the original request.
     */
    public synchronized nfs_resop4 replayLastResponse() throws ChimeraNFSException {
        if (lastFailure != null) {
            throw lastFailure;
        }
        if (lastResponse == null) {
            throw new BadSeqidException("no cached response for retransmitted sequence id");
        }
        return lastResponse;
    }

    /**
     * Consume the sequence id of a successfully executed request and cache its response for retransmissions.
     *
     * @param seqid sequence id of the request.
     * @param response the response sent to the client.
     */
    public synchronized void consumeSequence(seqid4 seqid, nfs_resop4 response) {
        seq = seqid.value;
        lastResponse = response;
        lastFailure = null;
    }

    /**
     * Consume the sequence id of a failed request, unless the error does not consume a sequence id, and record the
     * failure for retransmissions. The client does not advance its sequence id on the errors listed in
     * rfc7530#section-9.1.7 and retries a request answered with NFS4ERR_DELAY with the same sequence id.
     *
     * @param seqid sequence id of the request.
     * @param failure the failure sent to the client.
     */
    public synchronized void consumeSequenceOnFailure(seqid4 seqid, ChimeraNFSException failure) {
        switch (failure.getStatus()) {
            case nfsstat.NFSERR_BAD_SEQID:
            case nfsstat.NFSERR_BAD_STATEID:
            case nfsstat.NFSERR_STALE_CLIENTID:
            case nfsstat.NFSERR_STALE_STATEID:
            case nfsstat.NFSERR_BADXDR:
            case nfsstat.NFSERR_RESOURCE:
            case nfsstat.NFSERR_NOFILEHANDLE:
            case nfsstat.NFSERR_MOVED:
            case nfsstat.NFSERR_DELAY:
                return;
            default:
                seq = seqid.value;
                lastResponse = null;
                lastFailure = failure;
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
