/*
 * Copyright (c) 2009 - 2013 Deutsches Elektronen-Synchroton,
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

import java.lang.management.ManagementFactory;
import javax.management.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMX wrapper for google's Guava Cache
 */
public class GuavaCacheMXBeanImpl implements GuavaCacheMXBean {

    private final static Logger _log = LoggerFactory.getLogger(GuavaCacheMXBeanImpl.class);

    private final com.google.common.cache.Cache _cache;

    public GuavaCacheMXBeanImpl(String cname, com.google.common.cache.Cache cache) {
        _cache = cache;
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            String name = String.format("%s:type=Cache,name=%s",
                    _cache.getClass().getPackage().getName(), cname);
            ObjectName mxBeanName = new ObjectName(name);
            if (!server.isRegistered(mxBeanName)) {
                server.registerMBean(this, new ObjectName(name));
            }
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException
                | MBeanRegistrationException | NotCompliantMBeanException ex) {
            _log.warn("Failed to create JMX bean: {}", ex.getMessage());
        }
    }

    @Override
    public long getRequestCount() {
        return _cache.stats().requestCount();
    }

    @Override
    public long getHitCount() {
        return _cache.stats().hitCount();
    }

    @Override
    public double getHitRate() {
        return _cache.stats().hitRate();
    }

    @Override
    public long getMissCount() {
        return _cache.stats().missCount();
    }

    @Override
    public double getMissRate() {
        return _cache.stats().missRate();
    }

    @Override
    public long getLoadCount() {
        return _cache.stats().loadCount();
    }

    @Override
    public long getLoadSuccessCount() {
        return _cache.stats().loadSuccessCount();
    }

    @Override
    public long getLoadExceptionCount() {
        return _cache.stats().loadExceptionCount();
    }

    @Override
    public double getLoadExceptionRate() {
        return _cache.stats().loadExceptionRate();
    }

    @Override
    public long getTotalLoadTime() {
        return _cache.stats().totalLoadTime();
    }

    @Override
    public double getAverageLoadPenalty() {
        return _cache.stats().averageLoadPenalty();
    }

    @Override
    public long getEvictionCount() {
        return _cache.stats().evictionCount();
    }

    @Override
    public long getSize() {
        return _cache.size();
    }

    @Override
    public void cleanUp() {
        _cache.cleanUp();
    }

    @Override
    public void invalidateAll() {
        _cache.invalidateAll();
    }

}
