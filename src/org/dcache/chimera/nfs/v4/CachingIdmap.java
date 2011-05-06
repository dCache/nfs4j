/*
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
package org.dcache.chimera.nfs.v4;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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
    private final ConcurrentMap<String, Integer> _uidByNameCache;
    private final ConcurrentMap<String, Integer> _gidByNameCache;
    /*
     * reverse mapping cache
     */
    private final ConcurrentMap<Integer, String> _userNameByIdCache;
    private final ConcurrentMap<Integer, String> _groupNameByIdCache;

    /**
     * Construct caching {@link NfsIdMapping}.
     *
     * @param idmapd used as source.
     * @param size maximal number to cache.
     * @param timeout in seconds to cache successful results.
     */
    public CachingIdmap(NfsIdMapping idmapd, int size, long timeout) {
        _inner = idmapd;

        _uidByNameCache = new MapMaker().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                makeComputingMap( new ForwardUidMapping());

        _gidByNameCache  = new MapMaker().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                makeComputingMap( new ForwardGidMapping());

         _userNameByIdCache = new MapMaker().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                makeComputingMap(new ReverseUidMapping());

        _groupNameByIdCache = new MapMaker().
                expireAfterWrite(timeout, TimeUnit.SECONDS).
                softValues().
                maximumSize(size).
                makeComputingMap(new ReverseGidMapping());
    }

    @Override
    public String uidToPrincipal(int id) {
        return _userNameByIdCache.get(id);
    }

    @Override
    public String gidToPrincipal(int id) {
        return _groupNameByIdCache.get(id);
    }

    @Override
    public int principalToUid(String principal) {
        return _uidByNameCache.get(principal);
    }

    @Override
    public int principalToGid(String principal) {
        return _gidByNameCache.get(principal);
    }

    /*
     * Forward mapping functions which delegate to inner NfsIdMapping
     */
    private class ForwardGidMapping implements Function<String, Integer> {

        @Override
        public Integer apply(String s) {
            return _inner.principalToGid(s);
        }
    }

    private class ForwardUidMapping implements Function<String, Integer> {

        @Override
        public Integer apply(String s) {
            return _inner.principalToUid(s);
        }
    }

    /*
     * Reverse mapping functions which delegate to inner NfsIdMapping
     */
    private class ReverseUidMapping implements Function<Integer, String> {

        @Override
        public String apply(Integer id) {
            return _inner.uidToPrincipal(id);
        }
    }

    private class ReverseGidMapping implements Function<Integer, String> {

        @Override
        public String apply(Integer id) {
            return _inner.gidToPrincipal(id);
        }
    }
}
