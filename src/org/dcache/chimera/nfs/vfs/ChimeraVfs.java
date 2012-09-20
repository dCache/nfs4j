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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.dcache.chimera.DirectoryStreamHelper;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.HimeraDirectoryEntry;
import org.dcache.chimera.JdbcFs;
import org.dcache.chimera.UnixPermission;
import org.dcache.chimera.nfs.v4.xdr.nfsace4;

/**
 * Interface to a virtual file system.
 */
public class ChimeraVfs implements VirtualFileSystem {

    private final JdbcFs _fs;

    public ChimeraVfs(JdbcFs fs) {
        _fs = fs;
    }

    @Override
    public Inode getRootInode() throws IOException {
        return toInode(FsInode.getRoot(_fs));
    }

    @Override
    public Inode getInodeById(final byte[] fh) throws IOException {
        return toInode(_fs.inodeFromBytes(fh));
    }

    @Override
    public byte[] getInodeId(Inode inode) throws IOException {
        FsInode chimeraInode = toFsInode(inode);
        return  _fs.inodeToBytes(chimeraInode);
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode fsInode = parentFsInode.inodeOf(path);
        return toInode(fsInode);
    }

    @Override
    public Inode create(Inode parent, Stat.Type type, String path, int uid, int gid, int mode) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode fsInode = _fs.createFile(parentFsInode, path, uid, gid, mode | typeToChimera(type), typeToChimera(type));
        return toInode(fsInode);
    }

    @Override
    public Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode fsInode = parentFsInode.mkdir(path, uid, gid, mode);
        return toInode(fsInode);
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, int uid, int gid) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode linkInode = toFsInode(link);
        FsInode fsInode = _fs.createHLink(parentFsInode, linkInode, path);
        return toInode(fsInode);
    }

    @Override
    public Inode symlink(Inode parent, String path, String link, int uid, int gid, int mode) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode fsInode = _fs.createLink(parentFsInode, path, link);
        return toInode(fsInode);
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        FsInode fsInode = toFsInode(inode);
        return _fs.read(fsInode, offset, data, 0, count);
    }

    @Override
    public void move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        FsInode from = toFsInode(src);
        FsInode to = toFsInode(dest);
        _fs.move(from, oldName, to, newName);
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        FsInode fsInode = toFsInode(inode);
        int count = (int) fsInode.statCache().getSize();
        byte[] data = new byte[count];
        int n = _fs.read(fsInode, 0, data, 0, count);
        if (n < 0) {
            throw new IOException("Can't read symlink");
        }
        return new String(data, 0, n);
    }

    @Override
    public boolean remove(Inode parent, String path) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        return _fs.remove(parentFsInode, path);
    }

    @Override
    public int write(Inode inode, byte[] data, long offset, int count) throws IOException {
        FsInode fsInode = toFsInode(inode);
        return _fs.write(fsInode, offset, data, 0, count);
    }

    @Override
    public List<DirectoryEntry> list(Inode inode) throws IOException {
        FsInode parentFsInode = toFsInode(inode);
        List<HimeraDirectoryEntry> list = DirectoryStreamHelper.listOf(parentFsInode);
        return Lists.transform(list, new ChimeraDirectoryEntryToVfs());
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        return toInode(toFsInode(inode).getParent());
    }

    @Override
    public FsStat getFsStat() throws IOException {
        org.dcache.chimera.FsStat fsStat = _fs.getFsStat();
        return new FsStat(fsStat.getTotalSpace(),
                fsStat.getTotalFiles(),
                fsStat.getUsedSpace(),
                fsStat.getUsedFiles());
    }

    private FsInode toFsInode(Inode inode) throws IOException {
        ChimeraInode chimeraInode = (ChimeraInode)inode;
        return chimeraInode.inode;
    }

    private Inode toInode(final FsInode inode) {
        return new ChimeraInode(inode);
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        FsInode fsInode = toFsInode(inode);

        Stat stat =  fromChimeraStat(fsInode.stat());
        // bit of magic for  backward compatibility
        long id = fsInode.id();
        stat.setIno((int)id);
        stat.setDev((int)(id >> 32));
        return stat;
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        FsInode fsInode = toFsInode(inode);
        _fs.setInodeAttributes(fsInode, 0, toChimeraStat(stat));
    }
 
    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return new nfsace4[0];
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        // FIXME:
    }

    private static Stat fromChimeraStat(org.dcache.chimera.posix.Stat pStat) {
        Stat stat = new Stat();

        stat.setATime(pStat.getATime());
        stat.setCTime(pStat.getCTime());
        stat.setMTime(pStat.getMTime());

        stat.setGid(pStat.getGid());
        stat.setUid(pStat.getUid());
        stat.setDev(pStat.getDev());
        stat.setIno(pStat.getIno());
        stat.setMode(pStat.getMode());
        stat.setNlink(pStat.getNlink());
        stat.setRdev(pStat.getRdev());
        stat.setSize(pStat.getSize());

        return stat;
    }

    private static org.dcache.chimera.posix.Stat toChimeraStat(Stat stat) {
        org.dcache.chimera.posix.Stat pStat = new org.dcache.chimera.posix.Stat();

        pStat.setATime(stat.getATime());
        pStat.setCTime(stat.getCTime());
        pStat.setMTime(stat.getMTime());

        pStat.setGid(stat.getGid());
        pStat.setUid(stat.getUid());
        pStat.setDev(stat.getDev());
        pStat.setIno(stat.getIno());
        pStat.setMode(stat.getMode());
        pStat.setNlink(stat.getNlink());
        pStat.setRdev(stat.getRdev());
        pStat.setSize(stat.getSize());
        return pStat;
    }

    private static class ChimeraInode implements Inode {

        private final FsInode inode;

        ChimeraInode(FsInode inode) {
            this.inode = inode;
        }

        @Override
        public boolean exists() {
            return inode.exists();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ChimeraInode)) {
                return false;
            }
            ChimeraInode other = (ChimeraInode) obj;
            return this.inode.equals(other.inode);
        }

        @Override
        public int hashCode() {
            return inode.hashCode();
        }

        @Override
        public String toString() {
            return inode.toString();
        }
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return mode;
    }

    private class ChimeraDirectoryEntryToVfs implements Function<HimeraDirectoryEntry, DirectoryEntry> {

        @Override
        public DirectoryEntry apply(HimeraDirectoryEntry e) {
            return new DirectoryEntry(e.getName(), new ChimeraInode(e.getInode()), fromChimeraStat(e.getStat()));
        }
    }

    private int typeToChimera(Stat.Type type) {
        switch (type) {
            case SYMLINK:
                return UnixPermission.S_IFLNK;
            case DIRECTORY:
                return UnixPermission.S_IFDIR;
            case SOCK:
                return UnixPermission.S_IFSOCK;
            case FIFO:
                return UnixPermission.S_IFIFO;
            case BLOCK:
                return UnixPermission.S_IFBLK;
            case CHAR:
                return UnixPermission.S_IFCHR;
            case REGULAR:
            default:
                return UnixPermission.S_IFREG;
        }
    }
}
