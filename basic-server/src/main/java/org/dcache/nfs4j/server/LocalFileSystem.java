package org.dcache.nfs4j.server;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.security.auth.Subject;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.status.ExistException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.NotEmptyException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.PermException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.status.StaleException;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.SimpleIdMap;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.vfs.AclCheckable;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.DirectoryStream;
import org.dcache.nfs.vfs.FsStat;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.Stat.Type;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;
import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.UnixNumericUserPrincipal;

/**
 *
 */
public class LocalFileSystem implements VirtualFileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystem.class);

    private final Path _root;
    private final FileStore _store;
    private final NonBlockingHashMap<Inode, Path> inodeToPath = new NonBlockingHashMap<>();
    private final NonBlockingHashMap<Path, Inode> pathToInode = new NonBlockingHashMap<>();
    private final Inode rootInode;
    private final NfsIdMapping _idMapper = new SimpleIdMap();
    private final UserPrincipalLookupService _lookupService =
            FileSystems.getDefault().getUserPrincipalLookupService();

    private final static boolean IS_UNIX;
    static {
        IS_UNIX = !System.getProperty("os.name").startsWith("Win");
    }

    private static Inode toFh(UUID inodeNumber) {
        return Inode.forFile(toByteArray(inodeNumber));
    }

    private Path resolveInode(Inode inodeNumber) throws ChimeraNFSException {
        Path path = inodeToPath.get(inodeNumber);
        if (path == null) {
            throw new StaleException("inode #" + inodeNumber);
        }
        return path;
    }

    private Inode resolvePath(Path path) throws NoEntException {
        Inode inodeNumber = pathToInode.get(path);
        if (inodeNumber == null) {
            if (!Files.exists(path)) {
                throw new NoEntException("path " + path);
            }
            inodeNumber = newInode();
            map(inodeNumber, path);
        }
        return inodeNumber;
    }

    private static Inode newInode() {
        return toFh(UUID.randomUUID());
    }

    private static byte[] toByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    /**
     * Map an inode number to a path.
     *
     * @param inodeNumber the inode number
     * @param path the path
     * @param force if true, overwrite any existing mapping
     */
    private void map(Inode inodeNumber, Path path, boolean force) {
        if (inodeToPath.putIfAbsent(inodeNumber, path) != null) {
            throw new IllegalStateException();
        }

        if (force) {
            pathToInode.put(path, inodeNumber);
        } else {
            Inode otherInodeNumber = pathToInode.putIfAbsent(path, inodeNumber);
            if (otherInodeNumber != null) {
                // try rollback
                if (inodeToPath.remove(inodeNumber) != path) {
                    throw new IllegalStateException("cant map, rollback failed");
                }
                throw new IllegalStateException("path " + path + " already mapped to " + otherInodeNumber);
            }
        }
    }

    private void map(Inode inodeNumber, Path path) {
        map(inodeNumber, path, false);
    }

    private void unmap(Inode inodeNumber, Path path) {
        Path removedPath = inodeToPath.remove(inodeNumber);
        if (!path.equals(removedPath)) {
            throw new IllegalStateException();
        }
        if (pathToInode.remove(path) != inodeNumber) {
            throw new IllegalStateException();
        }
    }

    private void remap(Inode inodeNumber, Path oldPath, Path newPath) {
        // TODO - attempt rollback?
        unmap(inodeNumber, oldPath);
        map(inodeNumber, newPath, true);
    }

    public LocalFileSystem(Path root, Iterable<FsExport> exportIterable) throws IOException {
        _root = root;
        assert (Files.exists(_root));
        _store = Files.getFileStore(_root);
        for (FsExport export : exportIterable) {
            String relativeExportPath = export.getPath().substring(1); // remove the opening '/'
            Path exportRootPath = root.resolve(relativeExportPath);
            if (!Files.exists(exportRootPath)) {
                Files.createDirectories(exportRootPath);
            }
        }

        UUID rootUUID = UUID.nameUUIDFromBytes(("LocalFileSystem:".concat(root.toString()))
                .getBytes(StandardCharsets.UTF_8));
        this.rootInode = toFh(rootUUID);
        map(rootInode, _root);

        // map existing structure (if any)
        Files.walkFileTree(_root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                FileVisitResult superRes = super.preVisitDirectory(dir, attrs);
                if (superRes != FileVisitResult.CONTINUE) {
                    return superRes;
                }
                if (dir.equals(_root)) {
                    return FileVisitResult.CONTINUE;
                }
                map(newInode(), dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                FileVisitResult superRes = super.visitFile(file, attrs);
                if (superRes != FileVisitResult.CONTINUE) {
                    return superRes;
                }
                map(newInode(), file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public Inode create(Inode parent, Type type, String path, Subject subject, int mode) throws IOException {
        Path parentPath = resolveInode(parent);
        Path newPath = parentPath.resolve(path);
        try {
            Files.createFile(newPath);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }
        Inode newInodeNumber = newInode();
        map(newInodeNumber, newPath);
        setOwnershipAndMode(newPath, subject, mode);
        return newInodeNumber;
    }

    @Override
    public FsStat getFsStat() throws IOException {
        long total = _store.getTotalSpace();
        long free = _store.getUsableSpace();
        return new FsStat(total, Long.MAX_VALUE, total - free, pathToInode.size());
    }

    @Override
    public Inode getRootInode() throws IOException {
        return rootInode;
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        // TODO - several issues
        // 2. we might accidentally allow composite paths here ("/dome/dir/down")
        // 3. we dont actually check that the parent exists
        Path parentPath = resolveInode(parent);
        Path child;
        if (path.equals(".")) {
            child = parentPath;
        } else if (path.equals("..")) {
            child = parentPath.getParent();
        } else {
            child = parentPath.resolve(path);
        }
        Inode childInodeNumber = resolvePath(child);
        return childInodeNumber;
    }

    @Override
    public Inode link(Inode parent, Inode existing, String target, Subject subject)
            throws IOException {
        Path parentPath = resolveInode(parent);
        Path existingPath = resolveInode(existing);
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

        Inode newInodeNumber = newInode();
        map(newInodeNumber, targetPath);
        return newInodeNumber;
    }

    @Override
    public DirectoryStream list(Inode inode, byte[] bytes, long l) throws IOException {
        Path path = resolveInode(inode);
        final List<DirectoryEntry> list = new ArrayList<>();
        try (java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            int cookie = 2; // first allowed cookie
            for (Path p : ds) {
                cookie++;
                if (cookie > l) {
                    Inode ino;
                    try {
                        ino = resolvePath(p);
                    } catch (NoEntException e) {
                        // File was briefly available, but deleted before we could allocate an inode
                        continue;
                    }
                    list.add(new DirectoryEntry(p.getFileName().toString(), ino, statPath(p, ino), cookie));
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
        Path parentPath = resolveInode(parent);
        Path newPath = parentPath.resolve(path);
        try {
            Files.createDirectory(newPath);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }
        Inode newInodeNumber = newInode();
        map(newInodeNumber, newPath);
        setOwnershipAndMode(newPath, subject, mode);
        return newInodeNumber;
    }

    private void setOwnershipAndMode(Path target, Subject subject, int mode) {
        if (!IS_UNIX) {
            // FIXME: windows must support some kind of file owhership as well
            return;
        }

        int uid = -1;
        int gid = -1;
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof UnixNumericUserPrincipal) {
                uid = (int) ((UnixNumericUserPrincipal) principal).longValue();
            }
            if (principal instanceof UnixNumericGroupPrincipal) {
                gid = (int) ((UnixNumericGroupPrincipal) principal).longValue();
            }
        }

        if (uid != -1) {
            try {
                Files.setAttribute(target, "unix:uid", uid, NOFOLLOW_LINKS);
            } catch (IOException e) {
                LOG.warn("Unable to chown file {}: {}", target, e.getMessage());
            }
        } else {
            LOG.warn("File created without uid: {}", target);
        }
        if (gid != -1) {
            try {
                Files.setAttribute(target, "unix:gid", gid, NOFOLLOW_LINKS);
            } catch (IOException e) {
                LOG.warn("Unable to chown file {}: {}", target, e.getMessage());
            }
        } else {
            LOG.warn("File created without gid: {}", target);
        }

        try {
            Files.setAttribute(target, "unix:mode", mode, NOFOLLOW_LINKS);
        } catch (IOException e) {
            LOG.warn("Unable to set mode of file {}: {}", target, e.getMessage());
        }
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        // TODO - several issues
        // 1. we might not deal with "." and ".." properly
        // 2. we might accidentally allow composite paths here ("/dome/dir/down")
        // 3. we return true (changed) even though in theory a file might be renamed to itself?
        Path currentParentPath = resolveInode(src);
        Path destPath = resolveInode(dest);
        Path currentPath = currentParentPath.resolve(oldName);
        Inode targetInodeNumber = resolvePath(currentPath);
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
        if (rootInode.equals(inode)) {
            throw new NoEntException("no parent"); // its the root
        }
        Path path = resolveInode(inode);
        Path parentPath = path.getParent();
        Inode parentInodeNumber = resolvePath(parentPath);
        return parentInodeNumber;
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        Path path = resolveInode(inode);
        ByteBuffer destBuffer = ByteBuffer.wrap(data, 0, count);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            return channel.read(destBuffer, offset);
        }
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        Path path = resolveInode(inode);
        return Files.readSymbolicLink(path).toString();
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
        Path parentPath = resolveInode(parent);
        Path targetPath = parentPath.resolve(path);
        Inode targetInodeNumber = resolvePath(targetPath);
        try {
            Files.delete(targetPath);
        } catch (DirectoryNotEmptyException e) {
            throw new NotEmptyException("dir " + targetPath + " is note empty", e);
        }
        unmap(targetInodeNumber, targetPath);
    }

    @Override
    public Inode symlink(Inode parent, String linkName, String targetName, Subject subject, int mode)
            throws IOException {
        Path parentPath = resolveInode(parent);
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

        Inode newInodeNumber = newInode();
        map(newInodeNumber, link);
        return newInodeNumber;
    }

    @Override
    public WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel)
            throws IOException {
        Path path = resolveInode(inode);
        ByteBuffer srcBuffer = ByteBuffer.wrap(data, 0, count);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            int bytesWritten = channel.write(srcBuffer, offset);
            return new WriteResult(StabilityLevel.FILE_SYNC, bytesWritten);
        }
    }

    @Override
    public void commit(Inode inode, long l, int i) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Stat statPath(Path p, Inode inodeNumber) throws IOException {

        Class<? extends BasicFileAttributeView> attributeClass =
                IS_UNIX ? PosixFileAttributeView.class : DosFileAttributeView.class;

        BasicFileAttributes attrs = Files.getFileAttributeView(p, attributeClass, NOFOLLOW_LINKS).readAttributes();

        Stat stat = new Stat();

        stat.setATime(attrs.lastAccessTime().toMillis());
        stat.setBTime(attrs.creationTime().toMillis());
        stat.setMTime(attrs.lastModifiedTime().toMillis());

        if (IS_UNIX) {
            stat.setGid((Integer) Files.getAttribute(p, "unix:gid", NOFOLLOW_LINKS));
            stat.setUid((Integer) Files.getAttribute(p, "unix:uid", NOFOLLOW_LINKS));
            stat.setMode((Integer) Files.getAttribute(p, "unix:mode", NOFOLLOW_LINKS));
            stat.setNlink((Integer) Files.getAttribute(p, "unix:nlink", NOFOLLOW_LINKS));
            stat.setCTime(((FileTime) Files.getAttribute(p, "unix:ctime", NOFOLLOW_LINKS)).toMillis());
        } else {
            DosFileAttributes dosAttrs = (DosFileAttributes) attrs;
            stat.setGid(0);
            stat.setUid(0);
            int type = dosAttrs.isSymbolicLink() ? Stat.S_IFLNK : dosAttrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
            stat.setMode(type | (dosAttrs.isReadOnly() ? 0400 : 0600));
            stat.setNlink(1);
        }

        stat.setDev(17);
        Long ino = longInoForInode(inodeNumber);
        if (ino != null) {
            stat.setIno(ino);
        }
        stat.setRdev(17);
        stat.setSize(attrs.size());
        stat.setGeneration(Math.max(stat.getCTime(), stat.getMTime()));

        return stat;
    }

    /**
     * Return a 64-bit inode value for the given {@link Inode}, or {@code null} if no such number is available. Not
     * returning a number is permissible per NFSv4, but not for NFSv3.
     * <p>
     * By default, the number is derived from the first 8 bytes of the Inode's fileId.
     *
     * @param inode The inode
     * @return The 64-bit inode, or {@code null} for "not available".
     */
    protected Long longInoForInode(Inode inode) {
        return Longs.fromByteArray(inode.getFileId());
    }

    @Override
    public int access(Subject subject, Inode inode, int mode) throws IOException {
        return mode;
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        Path path = resolveInode(inode);
        return statPath(path, inode);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        if (!IS_UNIX) {
            // FIXME: windows must support some kind of attribute update as well
            return;
        }

        Path path = resolveInode(inode);
        PosixFileAttributeView attributeView = Files.getFileAttributeView(path, PosixFileAttributeView.class,
                NOFOLLOW_LINKS);
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
                Files.setAttribute(path, "unix:mode", stat.getMode(), NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set mode unsupported: " + e.getMessage(), e);
            }
        }
        if (stat.isDefined(Stat.StatAttribute.SIZE)) {

            var currentAttributes = attributeView.readAttributes();

            if (currentAttributes.isDirectory()) {
                throw new IsDirException("set size on directory");
            }

            if (!Files.isRegularFile(path)) {
                throw new InvalException("set size on non file object");
            }

            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
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
        // NOP
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

    @Override
    public boolean getCaseInsensitive() {
        return true;
    }

    @Override
    public boolean getCasePreserving() {
        return true;
    }

}
