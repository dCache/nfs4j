/*
 * Copyright (c) 2017 - 2026 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;

import com.google.common.base.MoreObjects;

/**
 * Owner associated with the open/lock operations.
 */
public class StateOwner implements Serializable {

    private static final long serialVersionUID = -4712959403595550903L;

    /**
     * Reply to a sequenced nfsv4.0 request together with the current file handle and stateid as left by the operation,
     * to be restored when the reply is replayed.
     */
    record Reply(nfs_resop4 result, Inode currentInode, stateid4 currentStateid) {
    }

    /**
     * Per owner sequence to serialize opens with nfsv4.0. The last sequence id consumed by a request.
     */
    private int seq;

    /**
     * The reply to the request with the last consumed sequence id, see rfc7530#section-9.1.7.
     */
    private transient Reply reply;

    /**
     * Indicates that a request with the next sequence id is executing.
     */
    private transient boolean inProgress;

    /**
     * Client unique state owner.
     */
    private final state_owner4 owner;

    public StateOwner(state_owner4 owner, int seq) {
        this.owner = owner;
        this.seq = seq;
    }

    /**
     * Acquire the owner's sequence for a request with given sequence id. The value of {@code seqid} is compared to the
     * last consumed sequence id, see rfc7530#section-9.1.7:
     * <ul>
     * <li>If {@code seqid} is the next one, the request must be executed and {@code null} is returned. The result of
     * the execution must be recorded with {@link #update(seqid4, Reply)}.
     * <li>If {@code seqid} is the last consumed one, the request is a retransmission and the cached reply is returned.
     * As the client retries a request answered with NFS4ERR_DELAY with a new RPC transaction, the request is executed
     * again in that case.
     * <li>For all other values a {@link BadSeqidException} is thrown.
     * </ul>
     *
     * @param seqid the sequence id of the request.
     * @return the cached reply or {@code null} if the request must be executed.
     * @throws BadSeqidException if {@code seqid} is out of order.
     * @throws DelayException if a request with the same sequence id is still executing.
     */
    synchronized Reply acquire(seqid4 seqid) throws ChimeraNFSException {

        if (inProgress) {
            throw new DelayException("request with seqid " + seqid.value + " in progress");
        }

        if (seqid.value == seq && reply != null) {
            if (reply.result().getStatus() != nfsstat.NFSERR_DELAY) {
                return reply;
            }
            // re-execute as retry of a delayed request
        } else if (seqid.value != seq + 1) {
            throw new BadSeqidException("disordered : v/n : " + Integer.toHexString(seq + 1) +
                    "/" + Integer.toHexString(seqid.value));
        }

        inProgress = true;
        return null;
    }

    /**
     * Record the reply of a request executed after {@link #acquire(seqid4)}. The sequence id is consumed unless the
     * request failed with one of the errors on which the client doesn't advance the sequence id, see
     * rfc7530#section-9.1.7.
     *
     * @param seqid the sequence id of the request.
     * @param reply the reply sent to the client.
     */
    synchronized void update(seqid4 seqid, Reply reply) {
        inProgress = false;

        switch (reply.result().getStatus()) {
            case nfsstat.NFSERR_STALE_CLIENTID:
            case nfsstat.NFSERR_STALE_STATEID:
            case nfsstat.NFSERR_BAD_STATEID:
            case nfsstat.NFSERR_BAD_SEQID:
            case nfsstat.NFSERR_BADXDR:
            case nfsstat.NFSERR_RESOURCE:
            case nfsstat.NFSERR_NOFILEHANDLE:
            case nfsstat.NFSERR_MOVED:
                return;
            default:
                seq = seqid.value;
                this.reply = reply;
        }
    }

    /**
     * Release the sequence acquired by {@link #acquire(seqid4)} without consuming the sequence id, e.g. when the
     * request turned out to be a retransmission sequenced by another owner.
     */
    synchronized void release() {
        inProgress = false;
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
