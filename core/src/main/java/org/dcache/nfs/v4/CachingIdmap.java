/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import org.dcache.nfs.status.BadOwnerException;

/**
 * {@link NfsIdMapping} implementation which caches results from defined source.
 *
 * @since 0.0.4
 */
public class CachingIdmap implements NfsIdMapping {

    /**
     * Inner {@link NfsIdMapping} used as source by this CachingIdmap.
     */
    private final NfsIdMapping _inner;

    /*
     * forward mapping cache
     */
    private final LoadingCache<String, Integer> _uidByNameCache;
    private final LoadingCache<String, Integer> _gidByNameCache;
    /*
     * reverse mapping cache
     */
    private final LoadingCache<Integer, String> _userNameByIdCache;
    private final LoadingCache<Integer, String> _groupNameByIdCache;

    /**
     * Construct caching {@link NfsIdMapping}.
     *
     * @param idmapd used as source.
     * @param size maximal number to cache.
     * @param timeout in seconds to cache successful results.
     */
    public CachingIdmap(NfsIdMapping idmapd, int size, long timeout) {
        _inner = idmapd;

        _uidByNameCache = CacheBuilder.newBuilder().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                build( new ForwardUidMapping());

        _gidByNameCache  = CacheBuilder.newBuilder().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                build( new ForwardGidMapping());

         _userNameByIdCache = CacheBuilder.newBuilder().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                build(new ReverseUidMapping());

        _groupNameByIdCache = CacheBuilder.newBuilder().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                build(new ReverseGidMapping());
    }

    @Override
    public String uidToPrincipal(int id) {
        return _userNameByIdCache.getUnchecked(id);
    }

    @Override
    public String gidToPrincipal(int id) {
        return _groupNameByIdCache.getUnchecked(id);
    }

    @Override
    public int principalToUid(String principal) {
        return _uidByNameCache.getUnchecked(principal);
    }

    @Override
    public int principalToGid(String principal) {
        return _gidByNameCache.getUnchecked(principal);
    }

    /*
     * Forward mapping functions which delegate to inner NfsIdMapping
     */
    private class ForwardGidMapping extends CacheLoader<String, Integer> {

        @Override
        public Integer load(String s) throws BadOwnerException {
            return _inner.principalToGid(s);
        }
    }

    private class ForwardUidMapping extends CacheLoader<String, Integer> {

        @Override
        public Integer load(String s) throws BadOwnerException {
            return _inner.principalToUid(s);
        }
    }

    /*
     * Reverse mapping functions which delegate to inner NfsIdMapping
     */
    private class ReverseUidMapping extends CacheLoader<Integer, String> {

        @Override
        public String load(Integer id) {
            return _inner.uidToPrincipal(id);
        }
    }

    private class ReverseGidMapping extends CacheLoader<Integer, String> {

        @Override
        public String load(Integer id) {
            return _inner.gidToPrincipal(id);
        }
    }
}
