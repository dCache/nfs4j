/*
 * Copyright (c) 2017 - 2018 Deutsches Elektronen-Synchroton,
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

import com.google.common.io.BaseEncoding;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * An implementation of {@link LockManager} which uses Hazelcast's distributed
 * {@link MultiMap} to store locks.
 *
 * <p>
 * Example:
 *
 * <pre>
 *   HazelcastInstance hz = ...;
 *   LockManager lm1 = new DistributedLockManager(hz, "distributed-byte-range-lock");
 *   LockManager lm2 = new DistributedLockManager(hz, "distributed-byte-range-lock");
 * </pre>
 *
 * The {@code lm1} and {@code lm2} will share the same set of locks as long as they
 * connected to the same Hazelcast cluster.
 *
 * @since 0.16
 */
public class DistributedLockManager extends AbstractLockManager {

    private final MultiMap<String, NlmLock> locks;

    /**
     * Create a new {@code DistributedLockManager} with a given {@code name}.
     * The other instances with the same name will share the same back-end store and,
     * as a result, will see the same set of locks.
     *
     * @param hz reference to Haselcast instance.
     * @param name name of the lock manager.
     */
    public DistributedLockManager(HazelcastInstance hz, String name) {
        locks = hz.getMultiMap(name);
    }

    @Override
    protected Lock getObjectLock(byte[] objId) {
        String key = BaseEncoding.base16().upperCase().encode(objId);
        return new Lock() {
            @Override
            public void lock() {
                locks.lock(key);
            }

            @Override
            public void lockInterruptibly() throws InterruptedException {
                locks.tryLock(key, Long.MAX_VALUE, TimeUnit.DAYS);
            }

            @Override
            public boolean tryLock() {
                return locks.tryLock(key);
            }

            @Override
            public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                return locks.tryLock(key, time, unit);
            }

            @Override
            public void unlock() {
                locks.unlock(key);
            }

            @Override
            public Condition newCondition() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    /**
     * Get collection of currently used active locks on the object.
     * @param objId object id.
     * @return collection of active locks.
     */
    @Override
    protected Collection<NlmLock> getActiveLocks(byte[] objId) {
        String key = objIdToKey(objId);
        return locks.get(key);
    }

    @Override
    protected void add(byte[] objId, NlmLock lock) {
        String key = objIdToKey(objId);
        locks.put(key, lock);
    }

    @Override
    protected boolean remove(byte[] objId, NlmLock lock) {
        String key = objIdToKey(objId);
        return locks.remove(key, lock);
    }

    @Override
    protected void addAll(byte[] objId, Collection<NlmLock> locks) {
        String key = objIdToKey(objId);
        locks.forEach(l -> this.locks.put(key, l));
    }

    @Override
    protected void removeAll(byte[] objId, Collection<NlmLock> locks) {
        String key = objIdToKey(objId);
        locks.forEach(l -> this.locks.remove(key, l));
    }

    private static String objIdToKey(byte[] objId) {
        return BaseEncoding.base64().omitPadding().encode(objId);
    }

}
