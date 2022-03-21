/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CacheTest {

    private Cache<String, String> _cache;
    private ManualClock _clock;

    @Before
    public void setUp() {
        _clock = new ManualClock();
        _cache = new Cache<>("test cache", 10, TimeUnit.SECONDS.toMillis(5),
                TimeUnit.SECONDS.toMillis(5),
                new NopCacheEventListener(), _clock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCacheIdleBiggerThanMax() {
        new Cache<>("test cache", 10,
                5, 7,
                new NopCacheEventListener(), _clock);
    }

    @Test
    public void testPutGet() {

        _cache.put("key1", "value1");

        String value = _cache.get("key1");
        assertEquals("received object not equal", "value1", value);
    }

    @Test
    public void testGetAfterTimeout() throws Exception {

        _cache.put("key1", "value1");
        _clock.advance(6, TimeUnit.SECONDS);

        String value = _cache.get("key1");
        assertNull("Object not expired", value);
    }

    @Test
    public void testGetAfterRemove() throws Exception {

        _cache.put("key1", "value1");
        _cache.remove("key1");

        String value = _cache.get("key1");
        assertNull("Object not removed", value);
    }

    @Test
    public void testRemoveValid() throws Exception {
        _cache.put("key1", "value1");
        assertNotNull(_cache.remove("key1"));
    }

    @Test
    public void testRemoveInValid() throws Exception {
        _cache.put("key1", "value1");
        assertNull(_cache.remove("key2"));
    }

    @Test
    public void testRemoveExpired() throws Exception {
        _cache.put("key1", "value1");
        _clock.advance(6, TimeUnit.SECONDS);
        assertNull(_cache.remove("key1"));
    }

    @Test
    public void testExpiredByTime() throws Exception {
        _cache.put("key1", "value1");
        _clock.advance(_cache.getEntryIdleTime() + 1000, TimeUnit.MILLISECONDS);
        String value = _cache.get("key1");
        assertNull("Object not expired", value);
    }

    @Test
    public void testBigLifeTime() {
         _cache.put("key1", "value1", Long.MAX_VALUE, TimeUnit.SECONDS.toMillis(180));
          assertNotNull("Object expired", _cache.get("key1"));
    }

    @Test
    public void testCleanUp() {
        _cache.put("key1", "value1", 1000, 1000);
        _cache.put("key2", "value2", 600, 600);
        _clock.advance(700, TimeUnit.MILLISECONDS);
        _cache.cleanUp();
        assertEquals("unexpected number of elements", 1, _cache.size());
        assertNotNull("Expected Entry expired", _cache.get("key1"));
    }

    @Test
    public void testClear() {
        _cache.put("key1", "value1");
        _cache.put("key2", "value2");
        _cache.clear();
        assertTrue("Not all entries are removed", _cache.entries().isEmpty());
    }

    private static class ManualClock extends Clock {

        private final AtomicLong currentTime = new AtomicLong();

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(currentTime.get());
        }

        void advance(long time, TimeUnit unit) {
            currentTime.addAndGet(unit.toMillis(time));
        }

        @Override
        public ZoneId getZone() {
            return Clock.systemDefaultZone().getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedClassVersionError();
        }
    }
}