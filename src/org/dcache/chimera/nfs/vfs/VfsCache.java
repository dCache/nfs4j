/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.chimera.nfs.vfs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.dcache.chimera.nfs.v4.xdr.nfsace4;

/**
 * Caching decorator.
 */
public class VfsCache implements VirtualFileSystem {

    private final LoadingCache<CacheKey, Inode> _lookupCache;
    private final VirtualFileSystem _inner;

    public VfsCache(VirtualFileSystem inner) {
        _inner = inner;
        _lookupCache = CacheBuilder.newBuilder()
                .maximumSize(8192)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new FsCacheLoader());
    }

    @Override
    public int write(Inode inode, byte[] data, long offset, int count) throws IOException {
        return _inner.write(inode, data, offset, count);
    }

    @Override
    public Inode symlink(Inode parent, String path, String link, int uid, int gid, int mode) throws IOException {
        return _inner.symlink(parent, path, link, uid, gid, mode);
    }

    @Override
    public boolean remove(Inode parent, String path) throws IOException {
        final boolean removed = _inner.remove(parent, path);
        if (removed) {
            invalidateCache(parent, path);
        }
        return removed;
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
    public void move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        invalidateCache(src, oldName);
        invalidateCache(dest,newName);
        _inner.move(src, oldName, dest, newName);
    }

    @Override
    public Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws IOException {
        Inode inode = _inner.mkdir(parent, path, uid, gid, mode);
        updateCache(parent, path, inode);
        return inode;
    }

    @Override
    public List<DirectoryEntry> list(Inode inode) throws IOException {
        return _inner.list(inode);
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, int uid, int gid) throws IOException {
        Inode inode = _inner.link(parent, link, path, uid, gid);
        updateCache(parent, path, inode);
        return inode;
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        return getFromCache(parent, path);
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
        updateCache(parent, path, inode);
        return inode;

    }

    private Inode getFromCache(Inode parent, String path) throws IOException {
        try {
            return _lookupCache.getUnchecked(new CacheKey(parent, path));
        } catch (UncheckedExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            throw new IOException(e.getMessage(), t);
        }
    }

    private void invalidateCache(Inode parent, String path) {
        _lookupCache.invalidate(new CacheKey(parent, path));
    }

    private void updateCache(Inode parent, String path, Inode inode) {
        _lookupCache.asMap().put(new CacheKey(parent, path), inode);
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return _inner.access(inode, mode);
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        return _inner.getattr(inode);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        _inner.setattr(inode, stat);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return _inner.getAcl(inode);
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        _inner.setAcl(inode, acl);
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

    private class FsCacheLoader extends CacheLoader<CacheKey, Inode> {

        @Override
        public Inode load(CacheKey k) throws Exception {
            return _inner.lookup(k.getParent(), k.getName());
        }
    }
}
