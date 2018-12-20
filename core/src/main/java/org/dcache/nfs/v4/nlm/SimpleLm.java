/*
 * Copyright (c) 2015 - 2018 Deutsches Elektronen-Synchroton,
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
import com.google.common.util.concurrent.Striped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * Simple non-distributed implementation of {@link LockManager}.
 *
 * @since 0.14
 */
public class SimpleLm extends AbstractLockManager {

    /*
     * Use {@link Striped} here to split synchronized block on file locks into
     * multiple partitions to increase concurrency, while guaranteeing atomicity
     * on a single file.
     *
     * Use number of stripes equals to 4x#CPU. This matches to number of
     * worker threads configured by default.
     *
     * FIXME: get number of threads from RPC service.
     */
    private final Striped<Lock> objLock = Striped.lock(Runtime.getRuntime().availableProcessors() * 4);

    /**
     * Exclusive lock on objects locks.
     */
    private final ConcurrentHashMap<String, List<NlmLock>> locks = new ConcurrentHashMap<>();

    @Override
    protected Lock getObjectLock(byte[] objId) {
        String key = toKey(objId);
        return objLock.get(key);
    }

    @Override
    protected Collection<NlmLock> getActiveLocks(byte[] objId) {
        String key = toKey(objId);
        return locks.getOrDefault(key, Collections.emptyList());
    }

    @Override
    protected void add(byte[] objId, NlmLock lock) {
        String key = toKey(objId);
        Collection<NlmLock> l = locks.computeIfAbsent(key, k -> new ArrayList<>());
        l.add(lock);
    }

    @Override
    protected boolean remove(byte[] objId, NlmLock lock) {
        String key = toKey(objId);
        Collection<NlmLock> l = locks.get(key);
        boolean isRemoved = false;
        if (l != null) {
            isRemoved = l.remove(lock);
            if (l.isEmpty()) {
                locks.remove(key);
            }
        }
        return isRemoved;
    }

    @Override
    protected void addAll(byte[] objId, Collection<NlmLock> locks) {
        String key = toKey(objId);
        Collection<NlmLock> l = this.locks.computeIfAbsent(key, k -> new ArrayList<>());
        l.addAll(locks);
    }

    @Override
    protected void removeAll(byte[] objId, Collection<NlmLock> locks) {
        String key = toKey(objId);
        Collection<NlmLock> l = this.locks.get(key);
        if (l != null) {
            l.removeAll(locks);
            if (l.isEmpty()) {
                this.locks.remove(key);
            }
        }
    }

    private final String toKey(byte[] objId) {
        return BaseEncoding.base16().lowerCase().encode(objId);
    }

}
