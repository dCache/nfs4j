/*
 * Copyright (c) 2015 - 2017 Deutsches Elektronen-Synchroton,
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.utils.Opaque;

/**
 * Simple non-distributed implementation of {@link LockManager}.
 * @since 0.14
 */
public class SimpleLm implements LockManager {

    private final Multimap<Opaque, NlmLock> locks = ArrayListMultimap.create();

    @Override
    public synchronized void lock(byte[] objId, NlmLock lock) throws LockException {
        Opaque fh = new Opaque(objId);
        Collection<NlmLock> currentLocks = locks.get(fh);
        Optional<NlmLock> conflictingLock = currentLocks.stream()
                .filter(l -> l.isConflicting(lock))
                .findAny();

        if (conflictingLock.isPresent()) {
            throw new LockDeniedException("object locked", conflictingLock.get());
        }

        // no conflicting locks. try to merge existing locks
        List<NlmLock> toMerge = currentLocks.stream()
                .filter(l -> l.isOverlappingRange(lock))
                .filter(l -> l.isSameOwner(lock))
                .filter(l-> l.getLockType() == lock.getLockType())
                .collect(Collectors.toList());

        if(toMerge.isEmpty()) {
            locks.put(fh, lock);
        } else {
            // merge overlaping/continues locks
            long lockBegin = lock.getOffset();
            long lockEnd = lock.getLength() == nfs4_prot.NFS4_UINT64_MAX ? nfs4_prot.NFS4_UINT64_MAX : (lockBegin + lock.getLength());

            for(NlmLock l: toMerge) {
                lockBegin = Math.min(lockBegin, l.getOffset());
                lockEnd = lockEnd == nfs4_prot.NFS4_UINT64_MAX  || l.getLength() == nfs4_prot.NFS4_UINT64_MAX ?
                        nfs4_prot.NFS4_UINT64_MAX : Math.max(lockEnd, l.getOffset() + l.getLength() - 1);
            }
            NlmLock mergedLock = new NlmLock(lock.getOwner(), lock.getLockType(), lockBegin,
                    lockEnd == nfs4_prot.NFS4_UINT64_MAX ? lockEnd : lockEnd - lockBegin);
            currentLocks.removeAll(toMerge);
            locks.put(fh, mergedLock);
        }
    }

    @Override
    public synchronized void unlock(byte[] objId, NlmLock lock) throws LockException {
        Opaque fh = new Opaque(objId);
        Collection<NlmLock> currentLocks = locks.get(fh);

        // check for exact match first
        if (currentLocks.remove(lock)) {
            return;
        }

        List<NlmLock> toRemove = new ArrayList<>();
        List<NlmLock> toAdd = new ArrayList<>();

        currentLocks.stream()
                .filter(l -> l.isSameOwner(lock))
                .filter(l -> l.isOverlappingRange(lock))
                .forEach( l -> {
                    toRemove.add(l);
                        long l1 = lock.getOffset() - l.getOffset();
                        if (l1 > 0) {
                            NlmLock first = new NlmLock(l.getOwner(), l.getLockType(), l.getOffset(), l1);
                            toAdd.add(first);
                        }
                        if (lock.getLength() != nfs4_prot.NFS4_UINT64_MAX) {
                            long l2 = l.getLength() - l1 - 1;
                            if (l2 > 0) {
                                NlmLock second = new NlmLock(l.getOwner(), l.getLockType(),
                                        lock.getOffset() + lock.getLength(), l2);
                                toAdd.add(second);
                            }
                        }
                });

        if (toRemove.isEmpty()) {
            throw new LockRangeUnavailabeException("no matching lock");
        }
        currentLocks.removeAll(toRemove);
        currentLocks.addAll(toAdd);
    }

    @Override
    public synchronized void test(byte[] objId, NlmLock lock) throws LockException {
        Opaque fh = new Opaque(objId);
        Collection<NlmLock> currentLocks = locks.get(fh);
        Optional<NlmLock> conflictingLock = currentLocks.stream()
                .filter(l -> l.isOverlappingRange(lock) && !l.isSameOwner(lock))
                .findAny();

        if (conflictingLock.isPresent()) {
            throw new LockDeniedException("object locked", conflictingLock.get());
        }
    }

    @Override
    public synchronized void unlockIfExists(byte[] objId, NlmLock lock) {
        Opaque fh = new Opaque(objId);
        Collection<NlmLock> currentLocks = locks.get(fh);
        currentLocks.remove(lock);
    }
}
