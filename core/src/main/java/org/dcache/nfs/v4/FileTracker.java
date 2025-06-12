/*
 * Copyright (c) 2017 - 2025 Deutsches Elektronen-Synchroton,
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

import com.google.common.util.concurrent.Striped;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.status.DelegRevokedException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.ShareDeniedException;
import org.dcache.nfs.status.StaleException;
import org.dcache.nfs.util.AdaptiveDelegationLogic;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.open_delegation_type4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.util.Opaque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class which tracks open files.
 */
public class FileTracker {

    public static final Logger LOG = LoggerFactory.getLogger(FileTracker.class);

    /*
     * we use {@link Striped} locks here to split synchronized block on open files
     * into multiple partitions to increase concurrency, while guaranteeing atomicity
     * on a single file.
     *
     * we use number of stripes equals to 4x#CPU. This matches to number of
     * worker threads configured by default.
     *
     * FIXME: get number of threads from RPC service.
     */
    private final Striped<Lock> filesLock = Striped.lock(Runtime.getRuntime().availableProcessors()*4);
    private final Map<Opaque, List<OpenState>> files = new ConcurrentHashMap<>();

    /**
     * Delegation records associated with open files.
     */
    private final Map<Opaque, List<DelegationState>> delegations = new ConcurrentHashMap<>();

    /**
     * Heuristic to offer delegations.
     *
     * FIXME: for now we use a fixed sizes and timeout. THe best practice still should be identified.
     */
    private final AdaptiveDelegationLogic adlHeuristic =
            new AdaptiveDelegationLogic(4096, 4096, Duration.ofSeconds(120));


    private static class OpenState {

        private final NFS4Client client;
        private final stateid4 stateid;
        private final StateOwner owner;
        private int shareAccess;
        private int shareDeny;

        /**
         * Bitmask of share_access that have been seen by the open.
         * The bit position represents seen open mode.
         * <pre>
         *     1: OPEN4_SHARE_ACCESS_READ
         *     2: OPEN4_SHARE_ACCESS_WRITE
         *     3: OPEN4_SHARE_ACCESS_BOTH
         * </pre>
         */
        private int shareAccessSeen;

        /**
         * Bitmask of share_deny that have been seen by the open.
         * The bit position represents seen open mode.
         * <pre>
         *     1: OPEN4_SHARE_ACCESS_READ
         *     2: OPEN4_SHARE_ACCESS_WRITE
         *     3: OPEN4_SHARE_ACCESS_BOTH
         * </pre>
         */
        private int shareDenySeen;

        public OpenState(NFS4Client client, StateOwner owner, stateid4 stateid, int shareAccess, int shareDeny) {
            this.client = client;
            this.stateid = stateid;
            this.shareAccess = shareAccess;
            this.shareDeny = shareDeny;

            // initialize seen bitmaps with the current share modes, if set
            this.shareAccessSeen = shareAccess == 0 ? 0 : 1 << (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_BOTH) - 1;
            this.shareDenySeen = shareDeny == 0 ? 0 : 1 << (shareDeny & nfs4_prot.OPEN4_SHARE_DENY_BOTH) - 1;
            this.owner = owner;
        }

        public stateid4 getStateid() {
            return stateid;
        }

        public int getShareAccess() {
            return shareAccess;
        }

        public int getShareDeny() {
            return shareDeny;
        }

        public StateOwner getOwner() {
            return owner;
        }

        public NFS4Client getClient() {
            return client;
        }
    }

    /**
     * Open-delegation record
     */
    static final class DelegationState {
        private final NFS4Client client;
        private final NFS4State delegationStateid;
        private final int delegationType;
        private boolean revoked;

        /**
         * @param client
         * @param delegationStateid
         * @param delegationType
         */
        DelegationState(NFS4Client client, NFS4State delegationStateid, int delegationType) {
            this.client = client;
            this.delegationStateid = delegationStateid;
            this.delegationType = delegationType;
            this.revoked = false;
        }

        public NFS4Client client() {
            return client;
        }

        public NFS4State delegationStateid() {
            return delegationStateid;
        }

        public int delegationType() {
            return delegationType;
        }

        public boolean revoked() {
            return revoked;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (DelegationState) obj;
            return Objects.equals(this.client, that.client) &&
                    Objects.equals(this.delegationStateid, that.delegationStateid) &&
                    this.delegationType == that.delegationType &&
                    Objects.equals(this.revoked, that.revoked);
        }

        @Override
        public int hashCode() {
            return Objects.hash(client, delegationStateid, delegationType, revoked);
        }

        @Override
        public String toString() {
            return "DelegationState[" +
                    "client=" + client + ", " +
                    "delegationStateid=" + delegationStateid + ", " +
                    "delegationType=" + delegationType + ", " +
                    "revoked=" + revoked + ']';
        }


    }

    /**
     * Record associated with an open file.
     *
     * @param openStateId
     * @param delegationStateId
     * @param hasDelegation
     */
    public record OpenRecord(stateid4 openStateId, stateid4 delegationStateId, boolean hasDelegation) {

    }

    /**
     * Add a new open to the list of open files. If provided {@code shareAccess}
     * and {@code shareDeny} conflicts with existing opens, @{link ShareDeniedException}
     * exception will be thrown.
     * @param client nfs client performing the open operation.
     * @param owner open state owner
     * @param inode of opened file.
     * @param shareAccess type of access required.
     * @param shareDeny type of access to deny others.
     * @return a snapshot of an OpenRecord associated with open.
     * @throws ShareDeniedException if share reservation conflicts with an existing open.
     * @throws ChimeraNFSException
     */
    public OpenRecord addOpen(NFS4Client client, StateOwner owner, Inode inode, int shareAccess, int shareDeny) throws  ChimeraNFSException {

        // client explicitly refused delegation
        boolean acceptsDelegation = (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG) == 0;

        // client explicitly requested read delegation
        boolean wantReadDelegation = (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_READ_DELEG) != 0;

        // client explicitly requested write delegation
        boolean wantWriteDelegation = (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_WRITE_DELEG) != 0;

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {
            /*
             * check for existing opens on that file
             * initialize new array with size of one, as this is what the majority of cases will be
             */
            final List<OpenState> opens = files.computeIfAbsent(fileId, x -> new ArrayList<>(1));

            stateid4 stateid;
            // check for a conflicting open from not expired client (we need to check
            // client as session GC may not been active yet
            if (opens.stream()
                    .filter(o -> o.client.isLeaseValid())
                    .anyMatch(o -> (shareAccess & o.getShareDeny()) != 0|| (shareDeny & o.getShareAccess()) != 0)) {
                    throw new ShareDeniedException("Conflicting share");
            }

            /*
             * REVISIT: currently only read-delegations are supported
             */
            var existingDelegations = delegations.get(fileId);

            /*
             * delegation is possible if:
             * - client has not explicitly requested no delegation
             * - client has a callback channel
             * - client does not have a delegation for this file
             * - no other open has write access
             */
            boolean canDelegateRead = acceptsDelegation && (client.getCB() != null &&
                    (existingDelegations == null ||
                            existingDelegations.stream()
                                    .noneMatch(d -> d.client().getId() == client.getId())) &&
                            opens.stream()
                                    .noneMatch(os -> (os.shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) != 0));

            // recall any read delegations if write
            if ((existingDelegations != null) && (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) != 0) {
                var fh = new nfs_fh4(inode.toNfsHandle());
                int recalledDelegations = existingDelegations.stream()
                        .filter(d -> d.client().isLeaseValid())
                        .filter(d -> !d.client().getId().equals(client.getId()))
                        .reduce(0, (c, d) -> {
                            try {
                                d.client().getCB().cbDelegationRecall(fh, d.delegationStateid().stateid(), false);
                                d.revoked = true;
                                return c + 1;
                            } catch (IOException e) {
                                LOG.warn("Failed to recall delegation from {} : {}", d.client(), e.toString());
                                d.delegationStateid().disposeIgnoreFailures();
                                return c;
                            }
                        }, Integer::sum);

                if (recalledDelegations > 0) {
                    throw new DelayException("Recalling read delegations");
                }
            }

            // if there is another open from the same client we must merge
            // access mode and return the same stateid as required by rfc5661#18.16.3

            for (OpenState os : opens) {
                if (os.client.getId() == client.getId() && os.getOwner().equals(owner)) {
                        os.shareAccess |= shareAccess;
                        os.shareDeny |= shareDeny;

                        if (shareAccess != 0) {
                            os.shareAccessSeen |= 1 << ((shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_BOTH) - 1);
                        }
                        if (shareDeny != 0) {
                            os.shareDenySeen |= 1 << ((shareDeny & nfs4_prot.OPEN4_SHARE_ACCESS_BOTH) - 1);
                        }

                        os.stateid.seqid++;
                        //we need to return copy to avoid modification by concurrent opens
                        var openStateid = new stateid4(os.stateid.other, os.stateid.seqid);

                        // yet another open from the same client. Let's check if we can delegate.
                        if (canDelegateRead && (os.shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_BOTH) == nfs4_prot.OPEN4_SHARE_ACCESS_READ &&
                                (wantReadDelegation || adlHeuristic.shouldDelegate(client, inode))) {

                            var delegationState = client.createDelegationState(os.getOwner());
                            var delegation = new DelegationState(client, delegationState, open_delegation_type4.OPEN_DELEGATE_READ);
                            delegations.computeIfAbsent(fileId, x -> new ArrayList<>(1))
                                    .add(delegation);

                            return new OpenRecord(openStateid, delegationState.stateid(), true);
                        }

                        return new OpenRecord(openStateid, null, false);
                }
            }

            NFS4State state = client.createOpenState(owner);
            stateid = state.stateid();
            OpenState openState = new OpenState(client, owner, stateid, shareAccess, shareDeny);
            opens.add(openState);
            state.addDisposeListener(s -> removeOpen(inode, stateid));
            stateid.seqid++;

            //we need to return copy to avoid modification by concurrent opens
            var openStateid = new stateid4(stateid.other, stateid.seqid);

            // REVISIT: currently only read-delegations are supported
            if (canDelegateRead && (wantReadDelegation || adlHeuristic.shouldDelegate(client, inode))) {
                var delegationStateid = client.createDelegationState(state.getStateOwner());
                delegations.computeIfAbsent(fileId, x -> new ArrayList<>(1))
                        .add(new DelegationState(client, delegationStateid, open_delegation_type4.OPEN_DELEGATE_READ));
                return new OpenRecord(openStateid, delegationStateid.stateid(), true);
            } else {
                //we need to return copy to avoid modification by concurrent opens
                return new OpenRecord(openStateid, null, false);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reduce access on open file.
     *
     * @param client nfs client performing the open operation.
     * @param stateid associated with the open.
     * @param inode of opened file.
     * @param shareAccess type of access required.
     * @param shareDeny type of access to deny others.
     * @return a snapshot of the stateid associated with open.
     * @throws ChimeraNFSException
     */
    public stateid4 downgradeOpen(NFS4Client client, stateid4 stateid, Inode inode, int shareAccess, int shareDeny) throws ChimeraNFSException {

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {
            final List<OpenState> opens = files.get(fileId);

            OpenState os = opens.stream()
                    .filter(s -> client.getId() == s.client.getId())
                    .filter(s -> s.stateid.equals(stateid))
                    .findFirst()
                    .orElseThrow(BadStateidException::new);


            if ((os.shareAccess & shareAccess) != shareAccess) {
                throw new InvalException("downgrading to not owned share_access mode");
            }

            if ((os.shareDeny & shareDeny) != shareDeny) {
                throw new InvalException("downgrading to not owned share_deny mode");
            }

            // check if we are downgrading to a mode that has been seen
            if ((os.shareAccessSeen & (1 << (shareAccess - 1))) == 0) {
                throw new InvalException("downgrading to not seen share_access mode");
            }

            if ((os.shareDenySeen & (1 << (shareDeny - 1))) != 0) {
                throw new InvalException("downgrading to not seen share_deny mode");
            }

            os.shareAccess = shareAccess;
            os.shareDeny = shareDeny;

            os.stateid.seqid++;
            //we need to return copy to avoid modification by concurrent opens
            return new stateid4(os.stateid.other, os.stateid.seqid);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return delegation for the given file
     * @param client nfs client who returns the delegation.
     * @param stateid delegation stateid
     * @param inode the inode of the delegated file.
     */
    public void delegationReturn(NFS4Client client, stateid4 stateid, Inode inode)
            throws ChimeraNFSException {

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {

            var fileDelegations = delegations.get(fileId);
            if (fileDelegations == null) {
                throw new StaleException("no delegation found");
            }

            DelegationState delegation = fileDelegations.stream()
                    .filter(d -> d.client().getId().equals(client.getId()))
                    .filter(d -> d.delegationStateid().stateid().equals(stateid))
                    .findFirst()
                    .orElseThrow(StaleException::new);

            delegation.delegationStateid().tryDispose();
            fileDelegations.remove(delegation);
            if (fileDelegations.isEmpty()) {
                delegations.remove(fileId);
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Get access mode for a given files, client and stateid. The state is must be either an open,
     * lock or delegation stateid.
     *
     * @param client  nfs client who returns the delegation.
     * @param inode   the inode of the delegated file.
     * @param stateid open or delegation stateid
     */
    public int getShareAccess(NFS4Client client, Inode inode, stateid4 stateid)
            throws ChimeraNFSException {

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {

            switch (stateid.other[11]) {
                case Stateids.LOCK_STATE_ID:
                    NFS4State lockState = client.state(stateid);
                    stateid = lockState.getOpenState().stateid();
                    // fall through
                case Stateids.OPEN_STATE_ID: {
                    final List<OpenState> opens = files.get(fileId);

                    if (opens == null) {
                        throw new BadStateidException("no matching open");
                    }

                    final stateid4 openStateid = stateid;
                    return opens.stream()
                            .filter(s -> client.getId() == s.client.getId())
                            .filter(s -> s.stateid.equals(openStateid))
                            .mapToInt(OpenState::getShareAccess)
                            .findAny()
                            .orElseThrow(BadStateidException::new);
                }
                case Stateids.DELEGATION_STATE_ID: {

                    var fileDelegations = delegations.get(fileId);
                    if (fileDelegations == null) {
                        throw new BadStateidException("no delegation found");
                    }

                    stateid4 delegationStateid = stateid;

                    var delegation =  fileDelegations.stream()
                            .filter(d -> d.client().getId().equals(client.getId()))
                            .filter(d -> d.delegationStateid().stateid().equals(delegationStateid))
                            .findAny()
                            .orElseThrow(BadStateidException::new);

                    if (delegation.revoked()) {
                        throw new DelegRevokedException();
                    }
                    // NOTE: as delegation types match access modes we don't convert the values.
                    return delegation.delegationType();
                }

                default:
                    throw new BadStateidException();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove an open from the list.
     * @param inode of the opened file
     * @param stateid associated with the open.
     */
    void removeOpen(Inode inode, stateid4 stateid) {

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {
            final List<OpenState> opens = files.get(fileId);

            if (opens != null) {
                Iterator<OpenState> osi = opens.iterator();
                while(osi.hasNext()) {
                    OpenState os = osi.next();
                    if (os.stateid.equals(stateid)) {
                        osi.remove();
                        break;
                    }
                }

                /**
                 * As we hold the lock, nobody else have added something into it.
                 */
                if (opens.isEmpty()) {
                    files.remove(fileId);
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Get all currently open files with associated clients. The resulting map contains file's inodes
     * as key and collection of nfs clients that have this file opened as a value.
     *
     * @return map of all open files.
     */
    public Map<Inode, Collection<NFS4Client>> getOpenFiles() {
        return files.entrySet().stream()
              .collect(Collectors.toMap(
                    e -> Inode.forFile(e.getKey().getOpaque()),
                    e -> e.getValue().stream().map(OpenState::getClient).collect(Collectors.toSet()))
              );
    }


    /**
     * Get all currently issued delegations. The resulting map contains file's inodes
     * as key and collection of nfs clients that hold the delegation as a value.
     *
     * @return map of all currently issued delegations.
     */
    public Map<Inode, Collection<NFS4Client>> getDelegations() {
        return delegations.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Inode.forFile(e.getKey().getOpaque()),
                        e -> e.getValue().stream().map(DelegationState::client).collect(Collectors.toSet()))
                );
    }
}
