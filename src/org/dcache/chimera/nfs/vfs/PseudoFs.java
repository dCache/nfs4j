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

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import javax.security.auth.Subject;
import org.dcache.auth.Subjects;
import org.dcache.chimera.UnixPermission;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.vfs.Inode.Type;
import org.dcache.xdr.RpcCall;
import static org.dcache.chimera.nfs.v4.xdr.nfs4_prot.*;
import org.dcache.chimera.posix.Stat;

/**
 * A decorated {@code VirtualFileSystem} that builds a Pseudo file system
 * on top of an other file system based on export rules.
 *
 * In addition, PsudoFS takes the responsibility of permission and access checking.
 */
public class PseudoFs implements VirtualFileSystem {

    private final Subject _subject;
    private final InetAddress _inetAddress;
    private final VirtualFileSystem _inner;

    public PseudoFs(VirtualFileSystem inner, RpcCall call) {
        _inner = inner;
        _subject = call.getCredential().getSubject();
        _inetAddress = call.getTransport().getRemoteSocketAddress().getAddress();
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        int accessmask = accessModeToMask(mode);
        Stat stat = inode.stat();

        int unixAccessMask = unixToAccessmask(_subject, stat);

        // do & mode to remove all extra modes which was added during accessMaskToMode
        return accessMaskToMode(unixAccessMask & accessmask) & mode;
    }

    @Override
    public Inode create(Inode parent, Type type, String path, int uid, int gid, int mode) throws IOException {
        checkAccess(parent, ACE4_ADD_FILE);
        return _inner.create(parent, type, path, uid, gid, mode);
    }

    @Override
    public FsStat getFsStat() throws IOException {
        return _inner.getFsStat();
    }

    @Override
    public Inode getRootInode() throws IOException {
        return _inner.getRootInode();
    }

    @Override
    public Inode getInodeById(byte[] fh) throws IOException {
        FileHandle handle = new FileHandle(fh);
        return _inner.getInodeById(handle.getFsOpaque());
    }

    @Override
    public byte[] getInodeId(Inode inode) throws IOException {        
        byte[] opaque = _inner.getInodeId(inode);
        FileHandle.FileHandleBuilder builder = new FileHandle.FileHandleBuilder();
        return builder.build(opaque).bytes();
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        checkAccess(parent, ACE4_EXECUTE);
        return _inner.lookup(parent, path);
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, int uid, int gid) throws IOException {
        checkAccess(parent, ACE4_ADD_FILE);
        return _inner.link(parent, link, path, uid, gid);

    }

    @Override
    public List<DirectoryEntry> list(Inode inode) throws IOException {
        checkAccess(inode, ACE4_LIST_DIRECTORY);
        return _inner.list(inode);
    }

    @Override
    public Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws IOException {
        checkAccess(parent, ACE4_ADD_SUBDIRECTORY);
        return _inner.mkdir(parent, path, uid, gid, mode);
    }

    @Override
    public void move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        checkAccess(src, ACE4_DELETE_CHILD);
        checkAccess(dest, ACE4_ADD_FILE | ACE4_DELETE_CHILD);
        _inner.move(src, oldName, dest, newName);
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        return _inner.parentOf(inode);
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        checkAccess(inode, ACE4_READ_DATA);
        return _inner.read(inode, data, offset, count);
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_DATA);
        return _inner.readlink(inode);
    }

    @Override
    public boolean remove(Inode parent, String path) throws IOException {
        try {
            checkAccess(parent, ACE4_DELETE_CHILD);
        } catch (ChimeraNFSException e) {
            if (e.getStatus() == nfsstat.NFSERR_ACCESS) {
                Inode inode = _inner.lookup(parent, path);
                checkAccess(inode, ACE4_DELETE);
            } else {
                throw e;
            }
        }
        return _inner.remove(parent, path);
    }

    @Override
    public Inode symlink(Inode parent, String path, String link, int uid, int gid, int mode) throws IOException {
        checkAccess(parent, ACE4_ADD_FILE);
        return _inner.symlink(parent, path, link, uid, gid, mode);
    }

    @Override
    public int write(Inode inode, byte[] data, long offset, int count) throws IOException {
        checkAccess(inode, ACE4_WRITE_DATA);
        return _inner.write(inode, data, offset, count);
    }

    private void checkAccess(Inode inode, int requestedMask) throws IOException {
        Stat stat = inode.stat();

        if ((unixToAccessmask(_subject, stat) & requestedMask) != requestedMask) {
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "permission deny");
        }
    }

    /*
     * unix permission bits offset as defined in POSIX
     * for st_mode filed of the stat  structure.
     */
    private static final int BIT_MASK_OWNER_OFFSET = 6;
    private static final int BIT_MASK_GROUP_OFFSET = 3;
    private static final int BIT_MASK_OTHER_OFFSET = 0;
    static public final int RBIT = 04; // read bit
    static public final int WBIT = 02; // write bit
    static public final int XBIT = 01; //execute bit

    @SuppressWarnings("PointlessBitwiseExpression")
    private int unixToAccessmask(Subject subject, Stat stat) {
        int mode = stat.getMode();
        boolean isDir = (mode & UnixPermission.S_IFDIR) == UnixPermission.S_IFDIR;
        int fromUnixMask;

        if (Subjects.hasUid(subject, stat.getUid())) {
            fromUnixMask = toAccessMask(mode >> BIT_MASK_OWNER_OFFSET, isDir, true);
        } else if (Subjects.hasGid(subject, stat.getGid())) {
            fromUnixMask = toAccessMask(mode >> BIT_MASK_GROUP_OFFSET, isDir, false);
        } else {
            fromUnixMask = toAccessMask(mode >> BIT_MASK_OTHER_OFFSET, isDir, false);
        }
        return fromUnixMask;
    }

    private int toAccessMask(int mode, boolean isDir, boolean isOwner) {

        int mask = 0;

        if (isOwner) {
            mask |= ACE4_WRITE_ACL
                    | ACE4_WRITE_ATTRIBUTES;
        }

        if ((mode & RBIT) != 0) {
            mask |= ACE4_READ_DATA
                    | ACE4_READ_ACL
                    | ACE4_READ_ATTRIBUTES;
        }

        if ((mode & WBIT) != 0) {
            mask |= ACE4_WRITE_DATA
                    | ACE4_APPEND_DATA;

            if (isDir) {
                mask |= ACE4_DELETE_CHILD;
            }
        }

        if ((mode & XBIT) != 0) {
            mask |= ACE4_EXECUTE;

            if (isDir) {
                mask |= ACE4_LIST_DIRECTORY;
            }
        }

        return mask;
    }

    private int accessModeToMask(int accessMode) {

        int accessMask = 0;

        if (hasAccessBit(accessMode, ACCESS4_DELETE)) {
            accessMask |= ACE4_DELETE_CHILD;
        }

        if (hasAccessBit(accessMode, ACCESS4_EXECUTE)) {
            accessMask |= ACE4_EXECUTE;
        }

        if (hasAccessBit(accessMode, ACCESS4_EXTEND)) {
            accessMask |= ACE4_APPEND_DATA | ACE4_ADD_SUBDIRECTORY;
        }

        if (hasAccessBit(accessMode, ACCESS4_LOOKUP)) {
            accessMask |= ACE4_EXECUTE;
        }

        if (hasAccessBit(accessMode, ACCESS4_MODIFY)) {
            accessMask |= ACE4_WRITE_DATA;
        }

        if (hasAccessBit(accessMode, ACCESS4_READ)) {
            accessMask |= ACE4_READ_DATA;
        }

        return accessMask;
    }

    private int accessMaskToMode(int accessMask) {
        int mode = 0;

        if (hasAccessBit(accessMask, ACE4_READ_DATA)) {
            mode |= ACCESS4_READ;
        }

        if (hasAccessBit(accessMask, ACE4_EXECUTE)) {
            mode |= ACCESS4_LOOKUP;
            mode |= ACCESS4_EXECUTE;
        }

        if (hasAccessBit(accessMask, ACE4_WRITE_DATA)) {
            mode |= ACCESS4_MODIFY;
        }

        if (hasAccessBit(accessMask, ACE4_APPEND_DATA) || hasAccessBit(accessMask, ACE4_ADD_FILE)) {
            mode |= ACCESS4_EXTEND;
        }

        if (hasAccessBit(accessMask, ACE4_DELETE_CHILD)) {
            mode |= ACCESS4_DELETE;
        }

        return mode;
    }

    private boolean hasAccessBit(int accessMode, int bit) {
        return (accessMode & bit) == bit;
    }
}
