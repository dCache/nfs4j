/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.vfs;

import java.util.concurrent.TimeUnit;

/**
 * Class to hold {@link VfsCache} configuration.
 */
public class VfsCacheConfig {

    private int maxEntries = 0;
    private long lifeTime = 0;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private long fsStatLifeTime = 0;
    private TimeUnit fsStatTimeUnit = TimeUnit.HOURS;

    private long readdirLifeTime;
    private TimeUnit readdirLifeTimeUnit = TimeUnit.SECONDS;
    private int readdirMaxEntries;

    /**
     * Get maximal number of entries in the cache.
     * @return maximal number of entries
     */
    public int getMaxEntries() {
	return maxEntries;
    }

    /**
     * Get cached value validity time.
     * @return validity time
     */
    public long getLifeTime() {
	return lifeTime;
    }

    /**
     * Get unit in which cached value validity time is expressed.
     * @return time unit
     */
    public TimeUnit getTimeUnit() {
	return timeUnit;
    }

    /**
     * Get FsStat cached value validity time.
     * @return validity time
     */
    public long getFsStatLifeTime() {
        return fsStatLifeTime;
    }

    /**
     * Get unit in which FsStat cached value validity time is expressed.
     * @return time unit
     */
    public TimeUnit getFsSataTimeUnit() {
        return fsStatTimeUnit;
    }

     /**
     * Get directory listing cache validity time.
     *
     * @return cache validity time.
     */
    public long getReaddirLifeTime() {
        return readdirLifeTime;
    }

    /**
     * Get unit in which directory listing cache validity time is expressed.
     * @return time unit.
     */
    public TimeUnit getReaddirLifeTimeUnit() {
        return readdirLifeTimeUnit;
    }

    /**
     * Get maximal number of directory listings in the cache.
     * Note, that this number is independent from directory listing size.
     * @return maximal number of directory listings.
     */
    public int getReaddirMaxEntries() {
        return readdirMaxEntries;
    }

    /**
     * Set maximal number of entries in the cache.
     * @param maxEntries
     */
    public void setMaxEntries(int maxEntries) {
	this.maxEntries = maxEntries;
    }

    /**
     * Set cached value validity time.
     * @param lifeTime
     */
    public void setLifeTime(long lifeTime) {
	this.lifeTime = lifeTime;
    }

    /**
     * Set unit in which cached value validity time is expressed.
     * @param timeUnit
     */
    public void setTimeUnit(TimeUnit timeUnit) {
	this.timeUnit = timeUnit;
    }

    /**
     * Set FsStat cached value validity time.
     * @param lifeTime
     */
    public void setFsStatLifeTime(long lifeTime) {
        this.fsStatLifeTime = lifeTime;
    }

    /**
     * Set unit in which FsStat cached value validity time is expressed.
     * @param timeUnit
     */
    public void setFsStatTimeUnit(TimeUnit timeUnit) {
        this.fsStatTimeUnit = timeUnit;
    }

    /**
     * Set directory listing cache validity time.
     * @param lifeTime
     */
    public void setReaddirLifeTime(long lifeTime) {
        this.readdirLifeTime = lifeTime;
    }

    /**
     * Set unit in which directory listing cache validity time is expressed.
     * @param lifeTimeTimeUnit
     */
    public void setReaddirLifeTimeUnit(TimeUnit lifeTimeTimeUnit) {
        this.readdirLifeTimeUnit = lifeTimeTimeUnit;
    }

    /**
     * Set maximal number directory listings to cache.
     * @param maxEntries
     */
    public void setReaddirMaxEntries(int maxEntries) {
        readdirMaxEntries = maxEntries;
    }
}
