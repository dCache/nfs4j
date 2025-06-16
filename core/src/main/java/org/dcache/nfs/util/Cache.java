/*
 * Copyright (c) 2009 - 2022 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dictionary where value associated with the key may become unavailable due to validity timeout.
 *
 * Typical usage is: <pre>
 *     Cache&lt;String, String&gt; cache  = new Cache&lt;&gt;("test cache", 10, Duration.ofHours(1),
 *           Duration.ofMinutes(2);
 *
 *     cache.put("key", "value");
 *     String value = cache.get("key");
 *     if( value == null ) {
 *         System.out.println("entry expired");
 *     }
 *
 * </pre>
 *
 * @author Tigran Mkrtchyan
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
public class Cache<K, V> {

    private static final Logger _log = LoggerFactory.getLogger(Cache.class);
    private final Clock _timeSource;

    /**
     * The name of this cache.
     */
    private final String _name;

    /**
     * Maximum amount of time that an object is allowed to be cached. After expiration of this time cache entry
     * invalidated.
     */
    private final Duration _defaultEntryMaxLifeTime;

    /**
     * Time amount since last use of the object. After expiration of this time cache entry is invalidated.
     */
    private final Duration _defaultEntryIdleTime;

    /**
     * Maximum number of entries in cache.
     */

    private final int _size;

    /**
     * The storage.
     */
    private final Map<K, CacheElement<V>> _storage;

    /**
     * Internal storage access lock.
     */
    private final StampedLock _accessLock = new StampedLock();
    /**
     * Cache event listener.
     */
    private final CacheEventListener<K, V> _eventListener;

    /**
     * The JMX interface to this cache
     */
    private final CacheMXBean<V> _mxBean;

    /**
     * Last cleanup time
     */
    private final AtomicReference<Instant> _lastClean;

    /**
     * Create new cache instance with default {@link CacheEventListener} and default cleanup period.
     *
     * @param name Unique id for this cache.
     * @param size maximal number of elements.
     * @param entryLifeTime maximal time that an entry allowed to stay in the cache after creation.
     * @param entryIdleTime maximal time that an entry allowed to stay in the cache after last access.
     */
    public Cache(String name, int size, Duration entryLifeTime, Duration entryIdleTime) {
        this(name, size, entryLifeTime, entryIdleTime,
                new NopCacheEventListener<K, V>());
    }

    /**
     * Create new cache instance.
     *
     * @param name Unique id for this cache.
     * @param size maximal number of elements.
     * @param entryLifeTime maximal time that an entry allowed to stay in the cache after creation.
     * @param entryIdleTime maximal time that an entry allowed to stay in the cache after last access.
     * @param eventListener {@link CacheEventListener}
     */
    public Cache(final String name, int size, Duration entryLifeTime, Duration entryIdleTime,
            CacheEventListener<K, V> eventListener) {
        this(name, size, entryLifeTime, entryIdleTime, eventListener, Clock.systemDefaultZone());
    }

    /**
     * Create new cache instance.
     *
     * @param name Unique id for this cache.
     * @param size maximal number of elements.
     * @param entryLifeTime maximal time that an entry allowed to stay in the cache after creation.
     * @param entryIdleTime maximal time that an entry allowed to stay in the cache after last access.
     * @param eventListener {@link CacheEventListener}
     * @param clock {@link Clock} to use <code>timeValue</code> parameter.
     */
    public Cache(final String name, int size, Duration entryLifeTime, Duration entryIdleTime,
            CacheEventListener<K, V> eventListener, Clock clock) {

        checkArgument(entryLifeTime.compareTo(entryIdleTime) >= 0, "Entry life time cant be smaller that idle time");

        _name = name;
        _size = size;
        _defaultEntryMaxLifeTime = entryLifeTime;
        _defaultEntryIdleTime = entryIdleTime;
        _storage = new HashMap<>(_size);
        _eventListener = eventListener;
        _mxBean = new CacheMXBeanImpl<>(this);
        _timeSource = clock;
        _lastClean = new AtomicReference<>(_timeSource.instant());
    }

    /**
     * Get cache's name.
     *
     * @return name of the cache.
     */
    public String getName() {
        return _name;
    }

    /**
     * Put/Update cache entry.
     *
     * @param k key associated with the value.
     * @param v value associated with key.
     *
     * @throws MissingResourceException if Cache limit is reached.
     */
    public void put(K k, V v) {
        this.put(k, v, _defaultEntryMaxLifeTime, _defaultEntryIdleTime);
    }

    /**
     * Put/Update cache entry.
     *
     * @param k key associated with the value.
     * @param v value associated with key.
     * @param entryMaxLifeTime maximal time that an entry allowed to stay in the cache after creation.
     * @param entryIdleTime maximal time that an entry allowed to stay in the cache after last access.
     *
     * @throws MissingResourceException if Cache limit is reached.
     */
    public void put(K k, V v, Duration entryMaxLifeTime, Duration entryIdleTime) {
        _log.debug("Adding new cache entry: key = [{}], value = [{}]", k, v);

        long stamp = _accessLock.writeLock();
        try {
            if (_storage.size() >= _size && !_storage.containsKey(k)) {
                _log.warn("Cache limit reached: {}", _size);
                throw new MissingResourceException("Cache limit reached", Cache.class.getName(), "");
            }
            _storage.put(k, new CacheElement<>(v, _timeSource, entryMaxLifeTime, entryIdleTime));
        } finally {
            _accessLock.unlock(stamp);
        }

        _eventListener.notifyPut(this, v);
    }

    /**
     * Get stored value. If {@link Cache} does not have the associated entry or entry live time is expired
     * <code>null</code> is returned.
     *
     * @param k key associated with entry.
     * @return cached value associated with specified key.
     */
    public V get(K k) {

        V v;
        boolean valid;
        boolean removed = false;

        long stamp = _accessLock.readLock();
        try {
            CacheElement<V> element = _storage.get(k);

            if (element == null) {
                _log.debug("No cache hits for key = [{}]", k);
                return null;
            }

            valid = element.validAt(_timeSource.instant());
            v = element.getObject();

            if (!valid) {
                _log.debug("Cache hits but entry expired for key = [{}], value = [{}]", k, v);
                long ws = _accessLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                } else {
                    _accessLock.unlock(stamp);
                    stamp = _accessLock.writeLock();
                }
                removed = _storage.remove(k) != null;
            } else {
                _log.debug("Cache hits for key = [{}], value = [{}]", k, v);
            }
        } finally {
            _accessLock.unlock(stamp);
        }

        if (!valid) {
            // notify only if this thread have removed the expired entry
            if (removed) {
                _eventListener.notifyExpired(this, v);
            }
            v = null;
        } else {
            _eventListener.notifyGet(this, v);
        }
        return v;
    }

    /**
     * Remove entry associated with key.
     *
     * @param k key
     * @return valid entry associated with the key or null if key not found or expired.
     */
    public V remove(K k) {

        V v;
        boolean valid;

        long stamp = _accessLock.writeLock();
        try {
            CacheElement<V> element = _storage.remove(k);
            if (element == null)
                return null;
            valid = element.validAt(_timeSource.instant());
            v = element.getObject();
        } finally {
            _accessLock.unlock(stamp);
        }

        _log.debug("Removing entry: active = [{}] key = [{}], value = [{}]",
                valid, k, v);

        _eventListener.notifyRemove(this, v);

        return valid ? v : null;
    }

    /**
     * Get number of elements inside the cache.
     *
     * @return number of elements.
     */
    int size() {

        long stamp = _accessLock.readLock();
        try {
            return _storage.size();
        } finally {
            _accessLock.unlock(stamp);
        }
    }

    /**
     * Get maximal idle time until entry become unavailable.
     *
     * @return default amount of an entry's maximal idle time.
     */
    public Duration getEntryIdleTime() {
        return _defaultEntryIdleTime;
    }

    /**
     * Get maximal total time until entry become unavailable.
     *
     * @return default amount of an entry's live time.
     */
    public Duration getEntryLiveTime() {
        return _defaultEntryMaxLifeTime;
    }

    /**
     * Remove all values from the Cache. Notice, that remove notifications are not triggered.
     */
    public void clear() {

        _log.debug("Cleaning the cache");

        long stamp = _accessLock.writeLock();
        try {
            _storage.clear();
        } finally {
            _accessLock.unlock(stamp);
        }
    }

    /**
     * Check and remove expired entries.
     */
    public void cleanUp() {
        List<V> expiredEntries = new ArrayList<>();

        long stamp = _accessLock.writeLock();
        try {
            Instant now = _timeSource.instant();
            Iterator<Map.Entry<K, CacheElement<V>>> entries = _storage.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<K, CacheElement<V>> entry = entries.next();
                CacheElement<V> cacheElement = entry.getValue();

                if (!cacheElement.validAt(now)) {
                    _log.debug("Cleaning expired entry key = [{}], value = [{}]",
                            entry.getKey(), cacheElement.getObject());
                    entries.remove();
                    expiredEntries.add(cacheElement.getObject());
                }
            }
            _lastClean.set(now);
        } finally {
            _accessLock.unlock(stamp);
        }

        expiredEntries.forEach(v -> _eventListener.notifyExpired(this, v));
    }

    /**
     * Get {@link List<V>} of entries.
     *
     * @return list of entries.
     */
    public List<CacheElement<V>> entries() {
        List<CacheElement<V>> entries;

        long stamp = _accessLock.readLock();
        try {
            entries = new ArrayList<>(_storage.size());
            entries.addAll(_storage.values());
        } finally {
            _accessLock.unlock(stamp);
        }
        return entries;
    }

    public Instant lastClean() {
        return _lastClean.get();
    }
}
