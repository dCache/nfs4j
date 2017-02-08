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

/**
 */
public interface LockManager {

    /**
     * Lock byte range of an {@code objId}.
     * @param objId object to lock.
     * @param lock lock definition.
     * @throws LockDeniedException if a conflicting lock is detected.
     * @throws LockException if locking fails.
     */
    void lock(byte[] objId, NlmLock lock) throws LockException;

    /**
     * Unlock byte range of an {@code objId}.
     *
     * @param objId object to unlock.
     * @param lock lock definition.
     * @throws LockRangeUnavailabeException if no matching lock found.
     * @throws LockException if locking fails.
     */
    void unlock(byte[] objId, NlmLock lock) throws LockException;

    /**
     * Test byte range lock existence  for an {@code objId}. Same as {@link #lock},
     * except that a new lock is not created.
     *
     * @param objId object to lock.
     * @param lock lock definition.
     * @throws LockDeniedException if a conflicting lock is detected.
     * @throws LockException if locking fails.
     */
    void test(byte[] objId, NlmLock lock) throws LockException;

    /**
     * Like {@link #unlock(byte[], org.dcache.nfs.v4.nlm.NlmLock)}, but
     * does not fail if lock does not exists.
     * @param objId
     * @param lock
     */
    void unlockIfExists(byte[] objId, NlmLock lock);
}
