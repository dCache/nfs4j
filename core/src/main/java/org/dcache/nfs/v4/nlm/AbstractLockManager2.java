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

import org.dcache.nfs.util.Opaque;
import org.dcache.nfs.v4.xdr.nfs4_prot;

abstract class AbstractLockManager2 extends AbstractLockManager {
    private static final boolean DEBUG_DEPRECATED_CALLS = false;

    @Override
    @Deprecated(forRemoval = true)
    protected final Lock getObjectLock(byte[] objId) {
        throw new IllegalStateException();
    }

    @Override
    protected abstract Lock getObjectLock(Opaque objId);

    @Override
    @Deprecated(forRemoval = true)
    protected final Collection<NlmLock> getActiveLocks(byte[] objId) {
        throw new IllegalStateException();
    }

    @Override
    protected abstract Collection<NlmLock> getActiveLocks(Opaque objId);

    @Override
    @Deprecated(forRemoval = true)
    protected final void add(byte[] objId, NlmLock lock) {
        throw new IllegalStateException();
    }

    @Override
    protected abstract void add(Opaque objId, NlmLock lock);

    @Override
    @Deprecated(forRemoval = true)
    protected final boolean remove(byte[] objId, NlmLock lock) {
        throw new IllegalStateException();
    }

    @Override
    protected abstract boolean remove(Opaque objId, NlmLock lock);

    @Override
    @Deprecated(forRemoval = true)
    protected final void addAll(byte[] objId, Collection<NlmLock> locks) {
        throw new IllegalStateException();
    }

    @Override
    protected abstract void addAll(Opaque objId, Collection<NlmLock> locks);

    @Override
    @Deprecated(forRemoval = true)
    protected final void removeAll(byte[] objId, Collection<NlmLock> locks) {
        throw new IllegalStateException();
    }

    @Override
    protected abstract void removeAll(Opaque objId, Collection<NlmLock> locks);

    @Override
    @Deprecated(forRemoval = true)
    public void lock(byte[] objId, NlmLock lock) throws LockException {
        if (DEBUG_DEPRECATED_CALLS) {
            new IllegalStateException("Called deprecated method").printStackTrace();
        }
        lock(Opaque.forBytes(objId), lock);
    }

    @Override
    public void lock(Opaque objId, NlmLock lock) throws LockException {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            Collection<NlmLock> currentLocks = getActiveLocks(objId);
            Optional<NlmLock> conflictingLock = currentLocks.stream().filter((NlmLock l) -> l.isConflicting(lock))
                    .findAny();
            if (conflictingLock.isPresent()) {
                throw new LockDeniedException("object locked", conflictingLock.get());
            }
            // no conflicting locks. try to merge existing locks
            List<NlmLock> toMerge = currentLocks.stream().filter((NlmLock l) -> l.isOverlappingRange(lock)).filter((
                    NlmLock l) -> l.isSameOwner(lock)).filter((NlmLock l) -> l.getLockType() == lock.getLockType())
                    .collect(Collectors.toList());
            if (toMerge.isEmpty()) {
                add(objId, lock);
            } else {
                // merge overlaping/continues locks
                long lockBegin = lock.getOffset();
                long lockEnd = lock.getLength() == nfs4_prot.NFS4_UINT64_MAX ? nfs4_prot.NFS4_UINT64_MAX : (lockBegin
                        + lock.getLength());
                for (NlmLock l : toMerge) {
                    lockBegin = Math.min(lockBegin, l.getOffset());
                    lockEnd = lockEnd == nfs4_prot.NFS4_UINT64_MAX || l.getLength() == nfs4_prot.NFS4_UINT64_MAX
                            ? nfs4_prot.NFS4_UINT64_MAX : Math.max(lockEnd, l.getOffset() + l.getLength() - 1);
                }
                NlmLock mergedLock = new NlmLock(lock.getOwner(), lock.getLockType(), lockBegin,
                        lockEnd == nfs4_prot.NFS4_UINT64_MAX ? lockEnd : lockEnd - lockBegin);
                removeAll(objId, toMerge);
                add(objId, mergedLock);
            }
        } finally {
            dlmLock.unlock();
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    public void unlock(byte[] objId, NlmLock lock) throws LockException {
        if (DEBUG_DEPRECATED_CALLS) {
            new IllegalStateException("Called deprecated method").printStackTrace();
        }
        unlock(Opaque.forBytes(objId), lock);
    }

    @Override
    public void unlock(Opaque objId, NlmLock lock) throws LockException {
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
            currentLocks.stream().filter((NlmLock l) -> l.isSameOwner(lock)).filter((NlmLock l) -> l.isOverlappingRange(
                    lock)).forEach((NlmLock l) -> {
                        toRemove.add(l);
                        long l1 = lock.getOffset() - l.getOffset();
                        if (l1 > 0) {
                            NlmLock first = new NlmLock(l.getOwner(), l.getLockType(), l.getOffset(), l1);
                            toAdd.add(first);
                        }
                        if (lock.getLength() != nfs4_prot.NFS4_UINT64_MAX) {
                            long l2 = l.getLength() - l1 - 1;
                            if (l2 > 0) {
                                NlmLock second = new NlmLock(l.getOwner(), l.getLockType(), lock.getOffset() + lock
                                        .getLength(), l2);
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
    @Deprecated(forRemoval = true)
    public void test(byte[] objId, NlmLock lock) throws LockException {
        if (DEBUG_DEPRECATED_CALLS) {
            new IllegalStateException("Called deprecated method").printStackTrace();
        }
        test(Opaque.forBytes(objId), lock);
    }

    @Override
    public void test(Opaque objId, NlmLock lock) throws LockException {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            Collection<NlmLock> currentLocks = getActiveLocks(objId);
            Optional<NlmLock> conflictingLock = currentLocks.stream().filter((NlmLock l) -> l.isOverlappingRange(lock)
                    && !l.isSameOwner(lock)).findAny();
            if (conflictingLock.isPresent()) {
                throw new LockDeniedException("object locked", conflictingLock.get());
            }
        } finally {
            dlmLock.unlock();
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    public void unlockIfExists(byte[] objId, NlmLock lock) {
        if (DEBUG_DEPRECATED_CALLS) {
            new IllegalStateException("Called deprecated method").printStackTrace();
        }
        unlockIfExists(Opaque.forBytes(objId), lock);
    }

    @Override
    public void unlockIfExists(Opaque objId, NlmLock lock) {
        Lock dlmLock = getObjectLock(objId);
        dlmLock.lock();
        try {
            remove(objId, lock);
        } finally {
            dlmLock.unlock();
        }
    }
}
