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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.BaseEncoding;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple non-distributed implementation of {@link LockManager}.
 *
 * @since 0.14
 */
public class SimpleLm extends AbstractLockManager {

    /**
     * Exclusive lock on file object.
     */
    private final Lock objLock = new ReentrantLock();

    /**
     * Exclusive lock on objects locks.
     */
    private final Multimap<String, NlmLock> locks = ArrayListMultimap.create();

    @Override
    protected Lock getObjectLock(byte[] objId) {
        return objLock;
    }

    @Override
    protected Collection<NlmLock> getActiveLocks(byte[] objId) {
        String key = BaseEncoding.base16().lowerCase().encode(objId);
        return locks.get(key);
    }

    @Override
    protected void add(byte[] objId, NlmLock lock) {
        String key = BaseEncoding.base16().lowerCase().encode(objId);
        locks.put(key, lock);
    }

    @Override
    protected boolean remove(byte[] objId, NlmLock lock) {
        String key = BaseEncoding.base16().lowerCase().encode(objId);
        return locks.remove(key, lock);
    }

    @Override
    protected void addAll(byte[] objId, Collection<NlmLock> locks) {
        String key = BaseEncoding.base16().lowerCase().encode(objId);
        locks.forEach(l -> this.locks.put(key, l));
    }

    @Override
    protected void removeAll(byte[] objId, Collection<NlmLock> locks) {
        String key = BaseEncoding.base16().lowerCase().encode(objId);
        locks.forEach(l -> this.locks.remove(key, l));
    }

}
