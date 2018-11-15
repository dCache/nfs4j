/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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
package org.dcache.utils;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dictionary where value associated with the key may become unavailable due
 * to validity timeout.
 *
 * Typical usage is:
 * <pre>
 *     Cache&lt;String, String&gt; cache  = new Cache&lt;&gt;("test cache", 10, TimeUnit.HOURS.toMillis(1),
 *           TimeUnit.MINUTES.toMillis(5));
 *
 *     cache.put("key", "value");
 *     String value = cache.get("key");
 *     if( value == null ) {
 *         System.out.println("entry expired");
 *     }
 *
 * </pre>
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
     * Maximum allowed time, in milliseconds, that an object is allowed to be cached.
     * After expiration of this time cache entry invalidated.
     */

    private final long _defaultEntryMaxLifeTime;

    /**
     * Time in milliseconds since last use of the object. After expiration of this
     * time cache entry is invalidated.
     */
    private final long _defaultEntryIdleTime;

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
    private final Lock _accessLock = new ReentrantLock();
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
    private final AtomicLong _lastClean = new AtomicLong(System.currentTimeMillis());

    /**
     * Create new cache instance with default {@link CacheEventListener} and
     * default cleanup period.
     *
     * @param name Unique id for this cache.
     * @param size maximal number of elements.
     * @param entryLifeTime maximal time in milliseconds.
     * @param entryIdleTime maximal idle time in milliseconds.
     */
    public Cache(String name, int size, long entryLifeTime, long entryIdleTime) {
        this(name, size, entryLifeTime, entryIdleTime,
                new NopCacheEventListener<K, V>());
    }

    /**
     * Create new cache instance.
     *
     * @param name Unique id for this cache.
     * @param size maximal number of elements.
     * @param entryLifeTime maximal time in milliseconds.
     * @param entryIdleTime maximal idle time in milliseconds.
     * @param eventListener {@link CacheEventListener}
     */
    public Cache(final String name, int size, long entryLifeTime, long entryIdleTime,
            CacheEventListener<K, V> eventListener) {
        this(name, size, entryLifeTime, entryIdleTime, eventListener, Clock.systemDefaultZone());
    }

    /**
     * Create new cache instance.
     *
     * @param name Unique id for this cache.
     * @param size maximal number of elements.
     * @param entryLifeTime maximal time in milliseconds.
     * @param entryIdleTime maximal idle time in milliseconds.
     * @param eventListener {@link CacheEventListener}
     * @param clock {@link Clock} to use
     * <code>timeValue</code> parameter.
     */
    public Cache(final String name, int size, long entryLifeTime, long entryIdleTime,
            CacheEventListener<K, V> eventListener, Clock clock) {
        _name = name;
        _size = size;
        _defaultEntryMaxLifeTime = entryLifeTime;
        _defaultEntryIdleTime = entryIdleTime;
        _storage = new HashMap<>(_size);
        _eventListener = eventListener;
        _mxBean = new CacheMXBeanImpl<>(this);
        _timeSource = clock;
    }

    /**
     * Get cache's name.
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
     * @param entryMaxLifeTime maximal life time in milliseconds.
     * @param entryIdleTime maximal idle time in milliseconds.
     *
     * @throws MissingResourceException if Cache limit is reached.
     */
    public void put(K k, V v, long entryMaxLifeTime, long entryIdleTime) {
        _log.debug("Adding new cache entry: key = [{}], value = [{}]", k, v);

        _accessLock.lock();
        try {
            if( _storage.size() >= _size && !_storage.containsKey(k)) {
                _log.warn("Cache limit reached: {}", _size);
                throw new MissingResourceException("Cache limit reached", Cache.class.getName(), "");
            }
            _storage.put(k, new CacheElement<>(v, _timeSource, entryMaxLifeTime, entryIdleTime));
        } finally {
            _accessLock.unlock();
        }

        _eventListener.notifyPut(this, v);
    }

    /**
     * Get stored value. If {@link Cache} does not have the associated entry or
     * entry live time is expired <code>null</code> is returned.
     * @param k key associated with entry.
     * @return cached value associated with specified key.
     */
    public V get(K k) {

        V v;
        boolean valid;

        _accessLock.lock();
        try {
            CacheElement<V> element = _storage.get(k);

            if (element == null) {
                _log.debug("No cache hits for key = [{}]", k);
                return null;
            }

            long now = _timeSource.millis();
            valid = element.validAt(now);
            v = element.getObject();

            if ( !valid ) {
                _log.debug("Cache hits but entry expired for key = [{}], value = [{}]", k, v);
                _storage.remove(k);
            } else {
                _log.debug("Cache hits for key = [{}], value = [{}]", k, v);
            }
        } finally {
            _accessLock.unlock();
        }

        if(!valid) {
            _eventListener.notifyExpired(this, v);
            v = null;
        }else{
            _eventListener.notifyGet(this, v);
        }
        return v;
    }

    /**
     * Remove entry associated with key.
     *
     * @param k key
     * @return valid entry associated with the key or null if key not found or
     *   expired.
     */
    public V remove(K k) {

        V v;
        boolean valid;

        _accessLock.lock();
        try {
            CacheElement<V> element = _storage.remove(k);
            if( element == null ) return null;
            valid = element.validAt(_timeSource.millis());
            v = element.getObject();
        } finally {
            _accessLock.unlock();
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

        _accessLock.lock();
        try {
          return _storage.size();
        } finally {
            _accessLock.unlock();
        }
    }

    /**
     * Get maximal idle time until entry become unavailable.
     *
     * @return time in milliseconds.
     */
    public long getEntryIdleTime() {
        return _defaultEntryIdleTime;
    }

    /**
     * Get maximal total time until entry become unavailable.
     *
     * @return time in milliseconds.
     */
    public long getEntryLiveTime() {
        return _defaultEntryMaxLifeTime;
    }

    /**
     * Remove all values from the Cache. Notice, that remove notifications are not triggered.
     */
    public void clear() {

        _log.debug("Cleaning the cache");

        _accessLock.lock();
        try {
            _storage.clear();
        } finally {
            _accessLock.unlock();
        }
    }

    /**
     * Check and remove expired entries.
     */
    public void cleanUp() {
        List<V> expiredEntries = new ArrayList<>();

        _accessLock.lock();
        try {
            long now = _timeSource.millis();
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
            _accessLock.unlock();
        }

        expiredEntries.forEach( v -> _eventListener.notifyExpired(this, v));
    }

    /**
     * Get  {@link  List<V>} of entries.
     * @return list of entries.
     */
    public List<CacheElement<V>> entries() {
        List<CacheElement<V>> entries;

        _accessLock.lock();
        try {
            entries = new ArrayList<>(_storage.size());
            entries.addAll(_storage.values());
        } finally {
            _accessLock.unlock();
        }
        return entries;
    }

    public long lastClean() {
        return _lastClean.get();
    }
}
