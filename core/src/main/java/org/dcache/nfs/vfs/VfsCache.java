/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.utils.Opaque;

/**
 * Caching decorator.
 */
public class VfsCache implements VirtualFileSystem {

    private final Cache<CacheKey, Inode> _lookupCache;
    private final Cache<Opaque, Stat> _statCache;

    private final VirtualFileSystem _inner;

    public VfsCache(VirtualFileSystem inner, VfsCacheConfig cacheConfig) {
        _inner = inner;
	_lookupCache = CacheBuilder.newBuilder()
		.maximumSize(cacheConfig.getMaxEntries())
		.expireAfterWrite(cacheConfig.getLifeTime(), cacheConfig.getTimeUnit())
		.softValues()
		.build();

	_statCache = CacheBuilder.newBuilder()
		.maximumSize(cacheConfig.getMaxEntries())
		.expireAfterWrite(cacheConfig.getLifeTime(), cacheConfig.getTimeUnit())
		.softValues()
		.build();
    }

    @Override
    public int write(Inode inode, byte[] data, long offset, int count) throws IOException {
        return _inner.write(inode, data, offset, count);
    }

    @Override
    public Inode symlink(Inode parent, String path, String link, int uid, int gid, int mode) throws IOException {
        Inode inode = _inner.symlink(parent, path, link, uid, gid, mode);
	invalidateStatCache(parent);
	return inode;
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
	Inode inode = lookup(parent, path);
        _inner.remove(parent, path);
        invalidateLookupCache(parent, path);
	invalidateStatCache(parent);
	invalidateStatCache(inode);
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        return _inner.readlink(inode);
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        return _inner.read(inode, data, offset, count);
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        return _inner.parentOf(inode);
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {

        boolean isChanged = _inner.move(src, oldName, dest, newName);
	if (isChanged) {
	    invalidateLookupCache(src, oldName);
	    invalidateLookupCache(dest, newName);
	    invalidateStatCache(src);
	    invalidateStatCache(dest);
	}
	return isChanged;
    }

    @Override
    public Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws IOException {
        Inode inode = _inner.mkdir(parent, path, uid, gid, mode);
        updateLookupCache(parent, path, inode);
	invalidateStatCache(parent);
        return inode;
    }

    @Override
    public List<DirectoryEntry> list(Inode inode) throws IOException {
        return _inner.list(inode);
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, int uid, int gid) throws IOException {
        Inode inode = _inner.link(parent, link, path, uid, gid);
        updateLookupCache(parent, path, inode);
	invalidateStatCache(parent);
	invalidateStatCache(inode);
        return inode;
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        return lookupFromCacheOrLoad(parent, path);
    }

    @Override
    public Inode getRootInode() throws IOException {
        return _inner.getRootInode();
    }

    @Override
    public FsStat getFsStat() throws IOException {
        return _inner.getFsStat();
    }

    @Override
    public Inode create(Inode parent, Stat.Type type, String path, int uid, int gid, int mode) throws IOException {
        Inode inode = _inner.create(parent, type, path, uid, gid, mode);
        updateLookupCache(parent, path, inode);
	invalidateStatCache(parent);
        return inode;
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return _inner.access(inode, mode);
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        return statFromCacheOrLoad(inode);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        _inner.setattr(inode, stat);
	invalidateStatCache(inode);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return _inner.getAcl(inode);
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        _inner.setAcl(inode, acl);
    }

    @Override
    public boolean hasIOLayout(Inode inode) throws IOException {
        return _inner.hasIOLayout(inode);
    }

    @Override
    public AclCheckable getAclCheckable() {
        return _inner.getAclCheckable();
    }

    /*
       Utility methods for cache manipulation.
     */

    private void invalidateLookupCache(Inode parent, String path) {
	_lookupCache.invalidate(new CacheKey(parent, path));
    }

    private void updateLookupCache(Inode parent, String path, Inode inode) {
	_lookupCache.put(new CacheKey(parent, path), inode);
    }

    private void invalidateStatCache(final Inode inode) {
	_statCache.invalidate(new Opaque(inode.getFileId()));
    }

    private Inode lookupFromCacheOrLoad(final Inode parent, final String path) throws IOException {
	try {
	    return _lookupCache.get(new CacheKey(parent, path), new Callable<Inode>() {

		@Override
		public Inode call() throws Exception {
		    return _inner.lookup(parent, path);
		}

	    });
	} catch (ExecutionException e) {
	    Throwable t = e.getCause();
	    Throwables.propagateIfInstanceOf(t, IOException.class);
	    throw new IOException(e.getMessage(), t);
	}
    }

    private Stat statFromCacheOrLoad(final Inode inode) throws IOException {
	try {
	    return _statCache.get(new Opaque(inode.getFileId()), new Callable<Stat>() {

		@Override
		public Stat call() throws Exception {
		    return _inner.getattr(inode);
		}
	    });
	} catch (ExecutionException e) {
	    Throwable t = e.getCause();
	    Throwables.propagateIfInstanceOf(t, IOException.class);
	    throw new IOException(e.getMessage(), t);
	}
    }

    /**
     * Cache entry key based on parent id and name
     */
    private static class CacheKey {

        private final Inode _parent;
        private final String _name;

        public CacheKey(Inode parent, String name) {
            _parent = parent;
            _name = name;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CacheKey)) {
                return false;
            }

            final CacheKey other = (CacheKey) obj;
            return other._parent.equals(_parent)
                    & other._name.equals(_name);
        }

        @Override
        public int hashCode() {
            return _name.hashCode() ^ _parent.hashCode();
        }

        public String getName() {
            return _name;
        }

        public Inode getParent() {
            return _parent;
        }
    }
}
