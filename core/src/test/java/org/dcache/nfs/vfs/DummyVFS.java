/*
 * Copyright (c) 2019 Deutsches Elektronen-Synchroton,
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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.primitives.Longs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dcache.nfs.status.ExistException;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.NotEmptyException;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.SimpleIdMap;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.vfs.Stat.Type;

import javax.security.auth.Subject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.dcache.auth.GidPrincipal;
import org.dcache.auth.UidPrincipal;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.PermException;
import org.dcache.nfs.status.ServerFaultException;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.util.EnumSet;

/**
 * Stolen from https://github.com/kofemann/simple-nfs/blob/master/src/main/java/org/dcache/simplenfs/LocalFileSystem.java
 */
public class DummyVFS implements VirtualFileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DummyVFS.class);

    // UNIX permissions bits
    public static final int S_IRUSR = 00400; // owner has read permission
    public static final int S_IWUSR = 00200; // owner has write permission
    public static final int S_IXUSR = 00100; // owner has execute permission
    public static final int S_IRGRP = 00040; // group has read permission
    public static final int S_IWGRP = 00020; // group has write permission
    public static final int S_IXGRP = 00010; // group has execute permission
    public static final int S_IROTH = 00004; // others have read permission
    public static final int S_IWOTH = 00002; // others have write permission
    public static final int S_IXOTH = 00001; // others have execute

    private final Path _root;
    private final ConcurrentMap<Long, Path> inodeToPath = new ConcurrentHashMap<>();
    private final ConcurrentMap<Path, Long> pathToInode = new ConcurrentHashMap<>();
    private final AtomicLong fileId = new AtomicLong(1); //numbering starts at 1
    private final NfsIdMapping _idMapper = new SimpleIdMap();
    private final UserPrincipalLookupService _lookupService;
    private final FileSystem fs;

    public DummyVFS() throws IOException {

        fs = Jimfs.newFileSystem(Configuration
                .unix()
                .toBuilder()
                .setWorkingDirectory("/")
                .setAttributeViews("posix", "owner")
                .setDefaultAttributeValue("owner:owner", "0")
                .setDefaultAttributeValue("posix:group", "0")
                .build()
        );

        _root = fs.getPath("/");
        map(fileId.getAndIncrement(), _root); //so root is always inode #1
        _lookupService = fs.getUserPrincipalLookupService();
    }

    private Inode toFileHandle(long inodeNumber) {
        return Inode.forFile(Longs.toByteArray(inodeNumber));
    }

    private long toInodeNumber(Inode inode) {
        return Longs.fromByteArray(inode.getFileId());
    }

    private Path resolveInode(long inodeNumber) throws NoEntException {
        Path path = inodeToPath.get(inodeNumber);
        if (path == null) {
            throw new NoEntException("inode #" + inodeNumber);
        }
        return path;
    }

    private long resolvePath(Path path) throws NoEntException {
        Long inodeNumber = pathToInode.get(path);
        if (inodeNumber == null) {
            throw new NoEntException("path " + path);
        }
        return inodeNumber;
    }

    private void map(long inodeNumber, Path path) {
        if (inodeToPath.putIfAbsent(inodeNumber, path) != null) {
            throw new IllegalStateException();
        }
        Long otherInodeNumber = pathToInode.putIfAbsent(path, inodeNumber);
        if (otherInodeNumber != null) {
            //try rollback
            if (inodeToPath.remove(inodeNumber) != path) {
                throw new IllegalStateException("cant map, rollback failed");
            }
            throw new IllegalStateException("path ");
        }
    }

    private void unmap(long inodeNumber, Path path) {
        Path removedPath = inodeToPath.remove(inodeNumber);
        if (!path.equals(removedPath)) {
            throw new IllegalStateException();
        }
        if (pathToInode.remove(path) != inodeNumber) {
            throw new IllegalStateException();
        }
    }

    private void remap(long inodeNumber, Path oldPath, Path newPath) {
        //TODO - attempt rollback?
        unmap(inodeNumber, oldPath);
        map(inodeNumber, newPath);
    }

    @Override
    public Inode create(Inode parent, Type type, String path, Subject subject, int mode) throws IOException {
        long parentInodeNumber = toInodeNumber(parent);
        Path parentPath = resolveInode(parentInodeNumber);
        Path newPath = parentPath.resolve(path);
        try {
            Files.createFile(newPath);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }
        long newInodeNumber = fileId.getAndIncrement();
        map(newInodeNumber, newPath);
        setOwnershipAndMode(newPath, subject, mode);
        return toFileHandle(newInodeNumber);
    }

    @Override
    public FsStat getFsStat() throws IOException {
        FileStore store = Files.getFileStore(_root);
        long total = store.getTotalSpace();
        long free = store.getUsableSpace();
        return new FsStat(total, Long.MAX_VALUE, total - free, pathToInode.size());
    }

    @Override
    public Inode getRootInode() throws IOException {
        return toFileHandle(1); //always #1 (see constructor)
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        //TODO - several issues
        //2. we might accidentally allow composite paths here ("/dome/dir/down")
        //3. we dont actually check that the parent exists
        long parentInodeNumber = toInodeNumber(parent);
        Path parentPath = resolveInode(parentInodeNumber);
        Path child;
        if (path.equals(".")) {
            child = parentPath;
        } else if (path.equals("..")) {
            child = parentPath.getParent();
        } else {
            child = parentPath.resolve(path);
        }
        long childInodeNumber = resolvePath(child);
        return toFileHandle(childInodeNumber);
    }

    @Override
    public Inode link(Inode parent, Inode existing, String target, Subject subject) throws IOException {
        long parentInodeNumber = toInodeNumber(parent);
        Path parentPath = resolveInode(parentInodeNumber);

        long existingInodeNumber = toInodeNumber(existing);
        Path existingPath = resolveInode(existingInodeNumber);

        Path targetPath = parentPath.resolve(target);

        try {
            Files.createLink(targetPath, existingPath);
        } catch (UnsupportedOperationException e) {
            throw new NotSuppException("Not supported", e);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("Path exists " + target, e);
        } catch (SecurityException e) {
            throw new PermException("Permission denied: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ServerFaultException("Failed to create: " + e.getMessage(), e);
        }

        long newInodeNumber = fileId.getAndIncrement();
        map(newInodeNumber, targetPath);
        return toFileHandle(newInodeNumber);
    }

    @Override
    public DirectoryStream list(Inode inode, byte[] bytes, long l) throws IOException {
        long inodeNumber = toInodeNumber(inode);
        Path path = resolveInode(inodeNumber);
        final List<DirectoryEntry> list = new ArrayList<>();
        try ( java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            int cookie = 2; // first allowed cookie
            for (Path p : ds) {
                cookie++;
                if (cookie > l) {
                    long ino = resolvePath(p);
                    list.add(new DirectoryEntry(p.getFileName().toString(), toFileHandle(ino), statPath(p, ino), cookie));
                }
            }
        }
        return new DirectoryStream(list);
    }

    @Override
    public byte[] directoryVerifier(Inode inode) throws IOException {
        return DirectoryStream.ZERO_VERIFIER;
    }

    @Override
    public Inode mkdir(Inode parent, String path, Subject subject, int mode) throws IOException {
        long parentInodeNumber = toInodeNumber(parent);
        Path parentPath = resolveInode(parentInodeNumber);
        Path newPath = parentPath.resolve(path);
        try {
            Files.createDirectory(newPath);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }
        long newInodeNumber = fileId.getAndIncrement();
        map(newInodeNumber, newPath);
        setOwnershipAndMode(newPath, subject, mode);
        return toFileHandle(newInodeNumber);
    }

    private void setOwnershipAndMode(Path target, Subject subject, int mode) {

        Set<PosixFilePermission> permission = modeToPermissions(mode);

        int uid = -1;
        int gid = -1;
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof UidPrincipal) {
                uid = (int) ((UidPrincipal) principal).getUid();
            }
            if (principal instanceof GidPrincipal) {
                gid = (int) ((GidPrincipal) principal).getGid();
            }
        }

        if (uid != -1) {
            try {
                Files.setAttribute(target, "owner:owner", asUserPrincipal(uid), NOFOLLOW_LINKS);
            } catch (IOException e) {
                LOG.warn("Unable to chown file {}: {}", target, e.getMessage());
            }
        } else {
            LOG.warn("File created without uid: {}", target);
        }
        if (gid != -1) {
            try {
                Files.setAttribute(target, "posix:group", asGroupPrincipal(gid), NOFOLLOW_LINKS);
            } catch (IOException e) {
                LOG.warn("Unable to chown file {}: {}", target, e.getMessage());
            }
        } else {
            LOG.warn("File created without gid: {}", target);
        }

        try {
            Files.setAttribute(target, "posix:permissions", permission, NOFOLLOW_LINKS);
        } catch (IOException e) {
            LOG.warn("Unable to set mode of file {}: {}", target, e.getMessage());
        }
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        //TODO - several issues
        //1. we might not deal with "." and ".." properly
        //2. we might accidentally allow composite paths here ("/dome/dir/down")
        //3. we return true (changed) even though in theory a file might be renamed to itself?
        long currentParentInodeNumber = toInodeNumber(src);
        Path currentParentPath = resolveInode(currentParentInodeNumber);
        long destParentInodeNumber = toInodeNumber(dest);
        Path destPath = resolveInode(destParentInodeNumber);
        Path currentPath = currentParentPath.resolve(oldName);
        long targetInodeNumber = resolvePath(currentPath);
        Path newPath = destPath.resolve(newName);
        try {
            Files.move(currentPath, newPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }
        remap(targetInodeNumber, currentPath, newPath);
        return true;
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        long inodeNumber = toInodeNumber(inode);
        if (inodeNumber == 1) {
            throw new NoEntException("no parent"); //its the root
        }
        Path path = resolveInode(inodeNumber);
        Path parentPath = path.getParent();
        long parentInodeNumber = resolvePath(parentPath);
        return toFileHandle(parentInodeNumber);
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        long inodeNumber = toInodeNumber(inode);
        Path path = resolveInode(inodeNumber);
        ByteBuffer destBuffer = ByteBuffer.wrap(data, 0, count);
        try ( FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            return channel.read(destBuffer, offset);
        }
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        long inodeNumber = toInodeNumber(inode);
        Path path = resolveInode(inodeNumber);
        return Files.readSymbolicLink(path).toString();
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
        long parentInodeNumber = toInodeNumber(parent);
        Path parentPath = resolveInode(parentInodeNumber);
        Path targetPath = parentPath.resolve(path);
        long targetInodeNumber = resolvePath(targetPath);
        try {
            Files.delete(targetPath);
        } catch (DirectoryNotEmptyException e) {
            throw new NotEmptyException("dir " + targetPath + " is note empty", e);
        }
        unmap(targetInodeNumber, targetPath);
    }

    @Override
    public Inode symlink(Inode parent, String linkName, String targetName, Subject subject, int mode) throws IOException {
        long parentInodeNumber = toInodeNumber(parent);
        Path parentPath = resolveInode(parentInodeNumber);
        Path link = parentPath.resolve(linkName);
        Path target = parentPath.resolve(targetName);
        if (!targetName.startsWith("/")) {
            target = parentPath.relativize(target);
        }
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException e) {
            throw new NotSuppException("Not supported", e);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("Path exists " + linkName, e);
        } catch (SecurityException e) {
            throw new PermException("Permission denied: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ServerFaultException("Failed to create: " + e.getMessage(), e);
        }

        setOwnershipAndMode(link, subject, mode);

        long newInodeNumber = fileId.getAndIncrement();
        map(newInodeNumber, link);
        return toFileHandle(newInodeNumber);
    }

    @Override
    public WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel) throws IOException {
        long inodeNumber = toInodeNumber(inode);
        Path path = resolveInode(inodeNumber);
        ByteBuffer srcBuffer = ByteBuffer.wrap(data, 0, count);
        try ( FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            int bytesWritten = channel.write(srcBuffer, offset);
            return new WriteResult(StabilityLevel.FILE_SYNC, bytesWritten);
        }
    }

    @Override
    public void commit(Inode inode, long l, int i) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Stat statPath(Path p, long inodeNumber) throws IOException {

        Class<? extends BasicFileAttributeView> attributeClass = PosixFileAttributeView.class;

        BasicFileAttributes attrs = Files.getFileAttributeView(p, attributeClass, NOFOLLOW_LINKS).readAttributes();

        Stat stat = new Stat();

        stat.setATime(attrs.lastAccessTime().toMillis());
        stat.setCTime(attrs.creationTime().toMillis());
        stat.setMTime(attrs.lastModifiedTime().toMillis());

        stat.setGid(Integer.parseInt(((Principal) Files.getAttribute(p, "posix:group", NOFOLLOW_LINKS)).getName()));
        stat.setUid(Integer.parseInt(((Principal) Files.getAttribute(p, "owner:owner", NOFOLLOW_LINKS)).getName()));

        Set<PosixFilePermission> permissions = (Set<PosixFilePermission>) Files.getAttribute(p, "posix:permissions", NOFOLLOW_LINKS);
        stat.setMode(permissionsToMode(permissions));
//        stat.setNlink((Integer) Files.getAttribute(p, "nlink", NOFOLLOW_LINKS));
        stat.setDev(17);
        stat.setIno((int) inodeNumber);
        stat.setRdev(17);
        stat.setSize(attrs.size());
        stat.setFileid((int) inodeNumber);
        stat.setGeneration(attrs.lastModifiedTime().toMillis());

        return stat;
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return mode;
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        long inodeNumber = toInodeNumber(inode);
        Path path = resolveInode(inodeNumber);
        return statPath(path, inodeNumber);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {

        long inodeNumber = toInodeNumber(inode);
        Path path = resolveInode(inodeNumber);
        PosixFileAttributeView attributeView = Files.getFileAttributeView(path, PosixFileAttributeView.class, NOFOLLOW_LINKS);
        if (stat.isDefined(Stat.StatAttribute.OWNER)) {
            try {
                String uid = String.valueOf(stat.getUid());
                UserPrincipal user = _lookupService.lookupPrincipalByName(uid);
                attributeView.setOwner(user);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set uid failed: " + e.getMessage(), e);
            }
        }
        if (stat.isDefined(Stat.StatAttribute.GROUP)) {
            try {
                String gid = String.valueOf(stat.getGid());
                GroupPrincipal group = _lookupService.lookupPrincipalByGroupName(gid);
                attributeView.setGroup(group);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set gid failed: " + e.getMessage(), e);
            }
        }
        if (stat.isDefined(Stat.StatAttribute.MODE)) {
            try {
                Files.setAttribute(path, "posix:mode", stat.getMode(), NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set mode unsupported: " + e.getMessage(), e);
            }
        }
        if (stat.isDefined(Stat.StatAttribute.SIZE)) {
            try ( RandomAccessFile raf = new RandomAccessFile(path.toFile(), "w")) {
                raf.setLength(stat.getSize());
            }
        }
        if (stat.isDefined(Stat.StatAttribute.ATIME)) {
            try {
                FileTime time = FileTime.fromMillis(stat.getCTime());
                Files.setAttribute(path, "unix:lastAccessTime", time, NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set atime failed: " + e.getMessage(), e);
            }
        }
        if (stat.isDefined(Stat.StatAttribute.MTIME)) {
            try {
                FileTime time = FileTime.fromMillis(stat.getMTime());
                Files.setAttribute(path, "unix:lastModifiedTime", time, NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set mtime failed: " + e.getMessage(), e);
            }
        }
        if (stat.isDefined(Stat.StatAttribute.CTIME)) {
            try {
                FileTime time = FileTime.fromMillis(stat.getCTime());
                Files.setAttribute(path, "unix:ctime", time, NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set ctime failed: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return new nfsace4[0];
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        throw new UnsupportedOperationException("No ACL support");
    }

    @Override
    public boolean hasIOLayout(Inode inode) throws IOException {
        return false;
    }

    @Override
    public AclCheckable getAclCheckable() {
        return AclCheckable.UNDEFINED_ALL;
    }

    @Override
    public NfsIdMapping getIdMapper() {
        return _idMapper;
    }

    private static Principal asUserPrincipal(int uid) {
        return new UserPrincipal() {
            @Override
            public String getName() {
                return Integer.toString(uid);
            }
        };
    }

    private static Principal asGroupPrincipal(int gid) {
        return new GroupPrincipal() {
            @Override
            public String getName() {
                return Integer.toString(gid);
            }
        };
    }

    private static Set<PosixFilePermission> modeToPermissions(int mode) {

        Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);

        if ((mode & S_IRUSR) == S_IRUSR) {
            perms.add(PosixFilePermission.OWNER_READ);
        }
        if ((mode & S_IWUSR) == S_IWUSR) {
            perms.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((mode & S_IXUSR) == S_IXUSR) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        }

        // GROUP
        if ((mode & S_IRGRP) == S_IRGRP) {
            perms.add(PosixFilePermission.GROUP_READ);
        }
        if ((mode & S_IWGRP) == S_IWGRP) {
            perms.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((mode & S_IXGRP) == S_IXGRP) {
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        }

        // OTHERS
        if ((mode & S_IROTH) == S_IROTH) {
            perms.add(PosixFilePermission.OWNER_READ);
        }
        if ((mode & S_IWOTH) == S_IWOTH) {
            perms.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((mode & S_IXOTH) == S_IXOTH) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        }

        return perms;
    }

    private static int permissionsToMode(Set<PosixFilePermission> permissions) {

        int mode = 0;
        for (PosixFilePermission p : permissions) {
            switch (p) {
                case OWNER_READ:
                    mode |= S_IRUSR;
                    break;
                case OWNER_WRITE:
                    mode |= S_IWUSR;
                    break;
                case OWNER_EXECUTE:
                    mode |= S_IXUSR;
                    break;
                case GROUP_READ:
                    mode |= S_IRGRP;
                    break;
                case GROUP_WRITE:
                    mode |= S_IWGRP;
                    break;
                case GROUP_EXECUTE:
                    mode |= S_IXGRP;
                    break;
                case OTHERS_READ:
                    mode |= S_IROTH;
                    break;
                case OTHERS_WRITE:
                    mode |= S_IWOTH;
                    break;
                case OTHERS_EXECUTE:
                    mode |= S_IXOTH;
                    break;
            }
        }
        return mode;
    }
}
