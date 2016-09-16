/*
 * Copyright (c) 2016 Deutsches Elektronen-Synchroton,
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Striped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.ShareDeniedException;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.utils.Opaque;

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
    private final Map<Opaque, List<OpenState>> files = Collections.synchronizedMap(new HashMap<>());

    private static class OpenState {

        private final NFS4Client client;
        private final stateid4 stateid;
        private final state_owner4 owner;
        private int shareAccess;
        private int shareDeny;

        public OpenState(NFS4Client client, state_owner4 owner, stateid4 stateid, int shareAccess, int shareDeny) {
            this.client = client;
            this.stateid = stateid;
            this.shareAccess = shareAccess;
            this.shareDeny = shareDeny;
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

        public state_owner4 getOwner() {
            return owner;
        }

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
     * @return stateid associated with open.
     * @throws ShareDeniedException if share reservation conflicts with an existing open.
     * @throws ChimeraNFSException
     */
    public stateid4 addOpen(NFS4Client client, state_owner4 owner, Inode inode, int shareAccess, int shareDeny) throws  ChimeraNFSException {

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
            // check for conflickting open from not expired client (we need to check
            // client as session GC may not beed active yet
            if (opens.stream()
                    .filter(o -> o.client.isLeaseValid())
                    .anyMatch(o -> (shareAccess & o.getShareDeny()) != 0|| (shareDeny & o.getShareAccess()) != 0)) {
                    throw new ShareDeniedException("Conflicting share");
            }

            // if there is an another open from the same client we must merge
            // access mode and return the same stateid as required by rfc5661#18.16.3

            for (OpenState os : opens) {
                if (os.client.getId() == client.getId() &&
                        os.getOwner().equals(owner)) {
                        os.shareAccess |= shareAccess;
                        os.shareDeny |= shareDeny;
                        os.stateid.seqid.value++;
                        return os.stateid;
                }
            }

            NFS4State state = client.createState(owner);
            stateid = state.stateid();
            OpenState openState = new OpenState(client, owner, stateid, shareAccess, shareDeny);
            opens.add(openState);
            state.addDisposeListener(s -> removeOpen(inode, stateid));
            return stateid;
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
     * @return stateid associated with open.
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

            os.shareAccess = shareAccess;
            os.shareDeny = shareDeny;

            os.stateid.seqid.value++;
            return os.stateid;
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
                    .map(OpenState::getShareAccess)
                    .findFirst()
                    .orElseThrow(BadStateidException::new);
        } finally {
            lock.unlock();
        }
    }
}
