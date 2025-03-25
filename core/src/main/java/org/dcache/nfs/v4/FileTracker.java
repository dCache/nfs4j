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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.ShareDeniedException;
import org.dcache.nfs.status.StaleException;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.open_delegation_type4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.util.Opaque;

/**
 * A class which tracks open files.
 */
public class FileTracker {

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
     * Record associated with open-delegation.
     * @param client
     * @param stateid
     * @param delegationType
     */
    record DelegationState(NFS4Client client, stateid4 openStateId, stateid4 stateid, int delegationType) {

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

        boolean wantReadDelegation = (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WANT_READ_DELEG) != 0;
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

            // if there is another open from the same client we must merge
            // access mode and return the same stateid as required by rfc5661#18.16.3

            for (OpenState os : opens) {
                if (os.client.getId() == client.getId() &&
                        os.getOwner().equals(owner)) {
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
                        return new OpenRecord(openStateid, null, false);
                }
            }

            /*
             * REVISIT: currently only read-delegations are supported
             */
            var existingDelegations = delegations.get(fileId);

            /*
             * delegation is possible if:
             * - client has a callback channel
             * - client does not have a delegation for this file
             * - no other open has write access
             */
            boolean canDelegate = client.getCB() != null &&
                  (existingDelegations == null || existingDelegations.stream().noneMatch(d -> d.client().getId() == client.getId())) &&
                  opens.stream().noneMatch(os -> (os.shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) != 0);

            // recall any read delegations if write
            if ((existingDelegations != null) && (shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) != 0) {

                // REVISIT: usage of Stream#peek is an anti-pattern
                boolean haveRecalled = existingDelegations.stream()
                      .filter(d -> client.isLeaseValid())
                      .peek(d -> {
                          try {
                              d.client().getCB()
                                    .cbDelegationRecall(new nfs_fh4(inode.toNfsHandle()), d.stateid(), false);
                          } catch (IOException e) {
                              // ignore
                          }
                      }).count() > 0;

                if (haveRecalled) {
                    throw new DelayException("Recalling read delegations");
                }
            }

            NFS4State state = client.createState(owner);
            stateid = state.stateid();
            OpenState openState = new OpenState(client, owner, stateid, shareAccess, shareDeny);
            opens.add(openState);
            state.addDisposeListener(s -> removeOpen(inode, stateid));
            stateid.seqid++;

            //we need to return copy to avoid modification by concurrent opens
            var openStateid = new stateid4(stateid.other, stateid.seqid);

            // REVISIT: currently only read-delegations are supported
            if (wantReadDelegation && canDelegate) {
                // REVISIT: currently only read-delegations are supported
                stateid4 delegationStateid = client.createState(state.getStateOwner(), state).stateid();
                delegations.computeIfAbsent(fileId, x -> new ArrayList<>(1))
                        .add(new DelegationState(client, openStateid, delegationStateid, open_delegation_type4.OPEN_DELEGATE_READ));
                return new OpenRecord(openStateid, delegationStateid, true);
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
          throws StaleException {

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {

            var fileDelegations = delegations.get(fileId);
            if (fileDelegations == null) {
                throw new StaleException("no delegation found");
            }

            DelegationState delegation = fileDelegations.stream()
                    .filter(d -> d.client().getId() == client.getId())
                    .filter(d -> d.stateid().equals(stateid))
                    .findFirst()
                    .orElseThrow(StaleException::new);

            fileDelegations.remove(delegation);
            if (fileDelegations.isEmpty()) {
                delegations.remove(fileId);
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

            var existingDelegations = delegations.get(fileId);
            if (existingDelegations != null) {
                Iterator<DelegationState> dsi = existingDelegations.iterator();
                while (dsi.hasNext()) {
                    stateid4 os = dsi.next().openStateId();
                    if (os.equals(stateid)) {
                        dsi.remove();
                        break;
                    }
                }

                if (existingDelegations.isEmpty()) {
                    delegations.remove(fileId);
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Get open access type used by opened file.
     * @param client nfs client which performs the request.
     * @param inode of the opened file
     * @param stateid associated with the open.
     * @return share access typed used.
     * @throws BadStateidException if no open file associated with provided state id.
     */
    public int getShareAccess(NFS4Client client, Inode inode, stateid4 stateid) throws BadStateidException {

        Opaque fileId = new Opaque(inode.getFileId());
        Lock lock = filesLock.get(fileId);
        lock.lock();
        try {
            final List<OpenState> opens = files.get(fileId);

            if (opens == null) {
                throw new BadStateidException("no matching open");
            }

            return opens.stream()
                    .filter(s -> client.getId() == s.client.getId())
                    .filter(s -> s.stateid.equals(stateid))
                    .mapToInt(OpenState::getShareAccess)
                    .findFirst()
                    .orElseThrow(BadStateidException::new);
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
}
