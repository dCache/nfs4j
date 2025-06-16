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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * CacheElement wrapper.
 *
 * Keeps track elements creation and last access time.
 *
 * @param <V>
 */
public class CacheElement<V> {

    /**
     * Maximum amount of time that the cache entry is allowed to be cached. After expiration of this time cache entry
     * invalidated.
     */
    private final Duration _maxLifeTime;
    /**
     * Maximum amount of time that the cache entry is allowed to be cached since last use. After expiration of this time
     * cache entry is invalidated.
     */
    private final Duration _idleTime;
    /**
     * Element creation time.
     */
    private final Instant _creationTime;
    /**
     * Elements last access time.
     */
    private Instant _lastAccessTime;
    /**
     * internal object.
     */
    private final V _inner;

    private final Clock _clock;

    CacheElement(V inner, Clock clock, Duration maxLifeTime, Duration idleTime) {
        _clock = clock;
        _creationTime = _clock.instant();
        _lastAccessTime = _creationTime;
        _inner = inner;
        _maxLifeTime = maxLifeTime;
        _idleTime = idleTime;
    }

    /**
     * Get internal object stored in this element. This operation will update this element's last access time with the
     * current time.
     *
     * @return internal object.
     */
    public V getObject() {
        _lastAccessTime = _clock.instant();
        return _inner;
    }

    /**
     * Get internal object stored in this element. In opposite to {@link #getObject} the last access time of the element
     * will not be updated.
     *
     * @return internal object.
     */
    public V peekObject() {
        return _inner;
    }

    /**
     * Check the entry's validity at the specified point in time.
     *
     * @param instant point in time at which entry validity is checked.
     * @return true if entry still valid and false otherwise.
     */
    public boolean validAt(Instant instant) {
        return Duration.between(_lastAccessTime, instant).compareTo(_idleTime) <= 0 &&
                Duration.between(_creationTime, instant).compareTo(_maxLifeTime) <= 0;
    }

    @Override
    public String toString() {
        Instant now = _clock.instant();
        return String.format("Element: [%s], created: %s, last access: %s, life time %s, idle: %s, max idle: %s",
                _inner.toString(), _creationTime, _lastAccessTime,
                _maxLifeTime, Duration.between(_lastAccessTime, now), _idleTime);
    }
}
