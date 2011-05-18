/*
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
import java.util.Arrays;
import java.util.List;
import org.dcache.chimera.DirectoryStreamHelper;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.FsStat;
import org.dcache.chimera.HimeraDirectoryEntry;
import org.dcache.chimera.JdbcFs;
import org.dcache.chimera.UnixPermission;
import org.dcache.chimera.nfs.vfs.Inode.Type;
import org.dcache.chimera.nfs.NFSHandle;
import org.dcache.chimera.nfs.v4.xdr.nfsace4;
import org.dcache.chimera.posix.Stat;

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
    public Inode inodeOf(final byte[] fh) {
        return toInode(NFSHandle.toFsInode(_fs, fh));
    }

    @Override
    public Inode inodeOf(Inode parent, String path) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode fsInode = parentFsInode.inodeOf(path);
        return toInode(fsInode);
    }

    @Override
    public Inode create(Inode parent, Inode.Type type, String path, int uid, int gid, int mode) throws IOException {
        FsInode parentFsInode = toFsInode(parent);
        FsInode fsInode = _fs.createFile(parentFsInode, path, uid, gid, mode, typeToChimera(type));
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
        int count = (int) inode.statCache().getSize();
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
        try {
            List<HimeraDirectoryEntry> list = DirectoryStreamHelper.listOf(parentFsInode);
            return Lists.transform(list, new ChimeraDirectoryEntryToVfs());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public Inode parentOf(Inode inode) {
        return toInode(toFsInode(inode).getParent());
    }

    @Override
    public FsStat getFsStat() throws IOException {
        return _fs.getFsStat();
    }

    private FsInode toFsInode(Inode inode) {
        return NFSHandle.toFsInode(_fs, inode.toFileHandle());
    }

    private Inode toInode(final FsInode inode) {

        return new Inode() {

            @Override
            public byte[] toFileHandle() {
                return inode.toFullString().getBytes();
            }

            @Override
            public boolean exists() {
                return inode.exists();
            }

            @Override
            public Stat stat() throws IOException {
                return inode.stat();
            }

            @Override
            public Stat statCache() throws IOException {
                return inode.statCache();
            }

            @Override
            public int id() {
                return (int) inode.id();
            }

            @Override
            public void setATime(long time) throws IOException {
                inode.setATime(time);
            }

            @Override
            public void setCTime(long time) throws IOException {
                inode.setCTime(time);
            }

            @Override
            public void setGID(int id) throws IOException {
                inode.setGID(id);
            }

            @Override
            public void setMTime(long time) throws IOException {
                inode.setMTime(time);
            }

            @Override
            public void setMode(int size) throws IOException {
                inode.setMode(size);
            }

            @Override
            public void setSize(long size) throws IOException {
                inode.setSize(size);
            }

            @Override
            public void setUID(int id) throws IOException {
                inode.setUID(id);
            }

            @Override
            public nfsace4[] getAcl() throws IOException {
                return new nfsace4[0];
            }

            @Override
            public void setAcl(nfsace4[] acl) throws IOException {
                /* NOP */
            }

            @Override
            public Type type() {
                if (inode.isDirectory()) {
                    return Type.DIRECTORY;
                }
                if (inode.isLink()) {
                    return Type.SYMLINK;
                }
                return Type.REGULAR;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (!(obj instanceof Inode)) {
                    return false;
                }
                Inode other = (Inode) obj;
                return Arrays.equals(this.toFileHandle(), other.toFileHandle());
            }

            @Override
            public int hashCode() {
                return inode.hashCode();
            }
        };
    }

    private class ChimeraDirectoryEntryToVfs implements Function<HimeraDirectoryEntry, DirectoryEntry> {

        @Override
        public DirectoryEntry apply(HimeraDirectoryEntry e) {
            return new DirectoryEntry(e.getName(), toInode(e.getInode()), e.getStat());
        }
    }

    private int typeToChimera(Inode.Type type) {
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
