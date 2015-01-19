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
package org.dcache.nfs.vfs;

import java.io.IOException;
import java.util.List;
import javax.security.auth.Subject;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.xdr.nfsace4;

/**
 * A file system which forwards all its method calls to another file system.
 * Subclasses should override one or more methods to modify the behavior of the
 * backing file system as desired per the
 * <a href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * @sice 0.10
 */
public abstract class ForwardingFileSystem implements VirtualFileSystem {

    /**
     * Returns the backing delegate instance that methods are forwarded to.
     *
     * @return backing delegate instance
     */
    protected abstract VirtualFileSystem delegate();

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return delegate().access(inode, mode);
    }

    @Override
    public Inode create(Inode parent, Stat.Type type, String path, Subject subject, int mode) throws IOException {
        return delegate().create(parent, type, path, subject, mode);
    }

    @Override
    public FsStat getFsStat() throws IOException {
        return delegate().getFsStat();
    }

    @Override
    public Inode getRootInode() throws IOException {
        return delegate().getRootInode();
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        return delegate().lookup(parent, path);
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, Subject subject) throws IOException {
        return delegate().link(parent, link, path, subject);
    }

    @Override
    public List<DirectoryEntry> list(Inode inode) throws IOException {
        return delegate().list(inode);
    }

    @Override
    public Inode mkdir(Inode parent, String path, Subject subject, int mode) throws IOException {
        return delegate().mkdir(parent, path, subject, mode);
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        return delegate().move(src, oldName, dest, newName);
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        return delegate().parentOf(inode);
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        return delegate().read(inode, data, offset, count);
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        return delegate().readlink(inode);
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
        delegate().remove(parent, path);
    }

    @Override
    public Inode symlink(Inode parent, String path, String link, Subject subject, int mode) throws IOException {
        return delegate().symlink(parent, path, link, subject, mode);
    }

    @Override
    public WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel) throws IOException {
        return delegate().write(inode, data, offset, count, stabilityLevel);
    }

    @Override
    public void commit(Inode inode, long offset, int count) throws IOException {
        delegate().commit(inode, offset, count);
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        return delegate().getattr(inode);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        delegate().setattr(inode, stat);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return delegate().getAcl(inode);
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        delegate().setAcl(inode, acl);
    }

    @Override
    public boolean hasIOLayout(Inode inode) throws IOException {
        return delegate().hasIOLayout(inode);
    }

    @Override
    public AclCheckable getAclCheckable() {
        return delegate().getAclCheckable();
    }

    @Override
    public NfsIdMapping getIdMapper() {
        return delegate().getIdMapper();
    }

}
