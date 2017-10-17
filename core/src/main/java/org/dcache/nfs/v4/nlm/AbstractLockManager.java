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
package org.dcache.nfs.v4.nlm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import org.dcache.nfs.v4.xdr.nfs4_prot;

/**
 * An abstract implementation of {@link LockManager} that handles  lock
 * conflict, lock merge and lock split, but lets its subclasses to manage
 * thread safety and lock storage.
 *
 * @since 0.16
 */
public abstract class AbstractLockManager implements LockManager {

    /**
     * Get exclusive lock on objects locks.
     *
     * @param objId object id.
     * @return exclusive lock.
     */
    abstract protected Lock getObjectLock(byte[] objId);

    /**
     * Get collection of currently used active locks on the object.
     *
     * @param objId object id.
     * @return collection of active locks.
     */
    abstract protected Collection<NlmLock> getActiveLocks(byte[] objId);

    /**
     * Add {@code lock} to an object.
     * @param objId object id.
     * @param lock lock to add.
     */
    abstract protected void add(byte[] objId, NlmLock lock);

    /**
     * Remove a lock for the given object.
     * @param objId object id.
     * @param lock lock to remove.
     * @return true, if specified lock was removed.
     */
    abstract protected boolean remove(byte[] objId, NlmLock lock);

    /**
     * Add all locks from a given collection of locks
     * @param objId
     * @param locks
     */
    abstract protected void addAll(byte[] objId, Collection<NlmLock> locks);

    /**
     * Remove all locks specified by {@code locks} associated with the given
     * object.
     * @param objId object id.
     * @param locks collections of locks to remove.
     */
    abstract protected void removeAll(byte[] objId, Collection<NlmLock> locks);

    @Override
    public void lock(byte[] objId, NlmLock lock) throws LockException {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            Collection<NlmLock> currentLocks = getActiveLocks(objId);
            Optional<NlmLock> conflictingLock = currentLocks.stream().filter((NlmLock l) -> l.isConflicting(lock)).findAny();
            if (conflictingLock.isPresent()) {
                throw new LockDeniedException("object locked", conflictingLock.get());
            }
            // no conflicting locks. try to merge existing locks
            List<NlmLock> toMerge = currentLocks.stream().filter((NlmLock l) -> l.isOverlappingRange(lock)).filter((NlmLock l) -> l.isSameOwner(lock)).filter((NlmLock l) -> l.getLockType() == lock.getLockType()).collect(Collectors.toList());
            if (toMerge.isEmpty()) {
                add(objId, lock);
            } else {
                // merge overlaping/continues locks
                long lockBegin = lock.getOffset();
                long lockEnd = lock.getLength() == nfs4_prot.NFS4_UINT64_MAX ? nfs4_prot.NFS4_UINT64_MAX : (lockBegin + lock.getLength());
                for (NlmLock l : toMerge) {
                    lockBegin = Math.min(lockBegin, l.getOffset());
                    lockEnd = lockEnd == nfs4_prot.NFS4_UINT64_MAX || l.getLength() == nfs4_prot.NFS4_UINT64_MAX ? nfs4_prot.NFS4_UINT64_MAX : Math.max(lockEnd, l.getOffset() + l.getLength() - 1);
                }
                NlmLock mergedLock = new NlmLock(lock.getOwner(), lock.getLockType(), lockBegin, lockEnd == nfs4_prot.NFS4_UINT64_MAX ? lockEnd : lockEnd - lockBegin);
                removeAll(objId, toMerge);
                add(objId, mergedLock);
            }
        } finally {
            dlmLock.unlock();
        }
    }

    @Override
    public void unlock(byte[] objId, NlmLock lock) throws LockException {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            Collection<NlmLock> currentLocks = getActiveLocks(objId);
            // check for exact match first
            if (remove(objId, lock)) {
                return;
            }
            List<NlmLock> toRemove = new ArrayList<>();
            List<NlmLock> toAdd = new ArrayList<>();
            currentLocks.stream().filter((NlmLock l) -> l.isSameOwner(lock)).filter((NlmLock l) -> l.isOverlappingRange(lock)).forEach((NlmLock l) -> {
                toRemove.add(l);
                long l1 = lock.getOffset() - l.getOffset();
                if (l1 > 0) {
                    NlmLock first = new NlmLock(l.getOwner(), l.getLockType(), l.getOffset(), l1);
                    toAdd.add(first);
                }
                if (lock.getLength() != nfs4_prot.NFS4_UINT64_MAX) {
                    long l2 = l.getLength() - l1 - 1;
                    if (l2 > 0) {
                        NlmLock second = new NlmLock(l.getOwner(), l.getLockType(), lock.getOffset() + lock.getLength(), l2);
                        toAdd.add(second);
                    }
                }
            });
            if (toRemove.isEmpty()) {
                throw new LockRangeUnavailabeException("no matching lock");
            }
            removeAll(objId, toRemove);
            addAll(objId, toAdd);
        } finally {
            dlmLock.unlock();
        }
    }

    @Override
    public void test(byte[] objId, NlmLock lock) throws LockException {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            Collection<NlmLock> currentLocks = getActiveLocks(objId);
            Optional<NlmLock> conflictingLock = currentLocks.stream().filter((NlmLock l) -> l.isOverlappingRange(lock) && !l.isSameOwner(lock)).findAny();
            if (conflictingLock.isPresent()) {
                throw new LockDeniedException("object locked", conflictingLock.get());
            }
        } finally {
            dlmLock.unlock();
        }
    }

    @Override
    public void unlockIfExists(byte[] objId, NlmLock lock) {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            remove(objId, lock);
        } finally {
            dlmLock.unlock();
        }
    }

}
