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
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import org.dcache.auth.Subjects;
import org.dcache.chimera.UnixPermission;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.FsExport;
import org.dcache.chimera.nfs.PseudoFsNode;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.xdr.RpcCall;
import static org.dcache.chimera.nfs.v4.xdr.nfs4_prot.*;
import org.dcache.chimera.nfs.v4.xdr.nfsace4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A decorated {@code VirtualFileSystem} that builds a Pseudo file system
 * on top of an other file system based on export rules.
 *
 * In addition, PsudoFS takes the responsibility of permission and access checking.
 */
public class PseudoFs implements VirtualFileSystem {

    private final static Logger _log = LoggerFactory.getLogger(PseudoFs.class);
    private final Subject _subject;
    private final InetAddress _inetAddress;
    private final VirtualFileSystem _inner;
    private final ExportFile _exportFile;

    private final static int WANT_MODITY = ACE4_WRITE_ACL
            | ACE4_WRITE_ATTRIBUTES
            | ACE4_WRITE_DATA
            | ACE4_ADD_FILE
            | ACE4_DELETE_CHILD
            | ACE4_DELETE
            | ACE4_ADD_SUBDIRECTORY;

    public PseudoFs(VirtualFileSystem inner, RpcCall call, ExportFile exportFile) {
        _inner = inner;
        _subject = call.getCredential().getSubject();
        _inetAddress = call.getTransport().getRemoteSocketAddress().getAddress();
        _exportFile = exportFile;
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        int accessmask = accessModeToMask(mode);
        Stat stat = _inner.getattr(inode);

        int unixAccessMask = unixToAccessmask(_subject, stat);

        // do & mode to remove all extra modes which was added during accessMaskToMode
        return accessMaskToMode(unixAccessMask & accessmask) & mode;
    }

    @Override
    public Inode create(Inode parent, Stat.Type type, String path, int uid, int gid, int mode) throws IOException {
        checkAccess(parent, ACE4_ADD_FILE);
        return pushExportIndex(parent, _inner.create(parent, type, path, uid, gid, mode));
    }

    @Override
    public FsStat getFsStat() throws IOException {
        return _inner.getFsStat();
    }

    @Override
    public Inode getRootInode() throws IOException {
        return realToPseudo(_inner.getRootInode());
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        checkAccess(parent, ACE4_EXECUTE);
        if (parent.isPesudoInode()) {
            return lookupInPseudoDirectory(parent, path);
        }
        return pushExportIndex(parent, _inner.lookup(parent, path));
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, int uid, int gid) throws IOException {
        checkAccess(parent, ACE4_ADD_FILE);
        return pushExportIndex(parent, _inner.link(parent, link, path, uid, gid));

    }

    @Override
    public List<DirectoryEntry> list(Inode inode) throws IOException {
        checkAccess(inode, ACE4_LIST_DIRECTORY);
        if (inode.isPesudoInode()) {
            return listPseudoDirectory(inode);
        }
        return Lists.transform(_inner.list(inode), new PushParentIndex(inode));
    }

    @Override
    public Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws IOException {
        checkAccess(parent, ACE4_ADD_SUBDIRECTORY);
        return pushExportIndex(parent, _inner.mkdir(parent, path, uid, gid, mode));
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
                Inode inode = pushExportIndex(parent, _inner.lookup(parent, path));
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
        return pushExportIndex(parent, _inner.symlink(parent, path, link, uid, gid, mode));
    }

    @Override
    public int write(Inode inode, byte[] data, long offset, int count) throws IOException {
        checkAccess(inode, ACE4_WRITE_DATA);
        return _inner.write(inode, data, offset, count);
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_ATTRIBUTES);
        return _inner.getattr(inode);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        checkAccess(inode, ACE4_WRITE_ATTRIBUTES);
        _inner.setattr(inode, stat);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_ACL);
        return _inner.getAcl(inode);
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        checkAccess(inode, ACE4_WRITE_ACL);
        _inner.setAcl(inode, acl);
    }

    private boolean wantModify(int requestMask) {
        return (requestMask & WANT_MODITY) != 0;
    }

    private void checkAccess(Inode inode, int requestedMask) throws IOException {
        Stat stat = _inner.getattr(inode);

        if (inode.isPesudoInode() && wantModify(requestedMask)) {
            _log.warn("Access Deny: pseudo Inode {} {} {}", new Object[]{inode, requestedMask, _subject});
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "permission deny");
        }

        if (!inode.isPesudoInode()) {
            int exportIdx = inode.exportIndex();
            FsExport export = _exportFile.getExport(exportIdx, _inetAddress);
            if (exportIdx != 0 && export == null) {
                _log.warn("Access Deny to inode {} for client {}", new Object[]{inode, _inetAddress});
                throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "permission deny");
            }

            if ( (export.ioMode() == FsExport.IO.RO) && wantModify(requestedMask)) {
                _log.warn("Access Deny to modify (RO export) inode {} for client {}", new Object[]{inode, _inetAddress});
                throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "permission deny");
            }
        }

        int unixAccessmask = unixToAccessmask(_subject, stat);
        if ( (unixAccessmask & requestedMask) != requestedMask) {
            _log.warn("Access Deny: {} {} {} {}", new Object[] {inode, requestedMask, unixAccessmask, _subject});
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

        int mask = ACE4_READ_ATTRIBUTES; // we should always allow read rettribues on plane posix

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

    private Inode lookupInPseudoDirectory(Inode parent, String name) throws IOException {
        Set<PseudoFsNode> nodes = prepareExportTree();

        for (PseudoFsNode node : nodes) {
            if (node.id().equals(parent)) {
                PseudoFsNode n = node.getChild(name);
                if (n != null) {
                    return n.isMountPoint() ? pseudoIdToReal(n.id(), getIndexId(n)) : n.id();
                }
            }
        }
        throw new ChimeraNFSException(nfsstat.NFSERR_NOENT, "");
    }

    private Inode pseudoIdToReal(Inode inode, int index) {

        FileHandle fh = new FileHandle.FileHandleBuilder()
                .setExportIdx(index)
                .setType(0)
                .build(inode.getFileId());
        return new Inode(fh);
    }

    private int getIndexId(PseudoFsNode node) {
        List<FsExport> exports = node.getExports();
        return exports.get(0).getIndex();
    }

    private class ConvertToRealInode implements Function<DirectoryEntry, DirectoryEntry> {

        private final PseudoFsNode _node;

        ConvertToRealInode(PseudoFsNode node) {
            _node = node;
        }

        @Override
        public DirectoryEntry apply(DirectoryEntry input) {
            return new DirectoryEntry(input.getName(),
                    pseudoIdToReal(input.getInode(), getIndexId(_node)),
                    input.getStat());
        }
    }

    private class PushParentIndex implements Function<DirectoryEntry, DirectoryEntry> {

        private final Inode _inode;

        PushParentIndex(Inode parent) {
            _inode = parent;
        }

        @Override
        public DirectoryEntry apply(DirectoryEntry input) {
            return new DirectoryEntry(input.getName(),
                    pushExportIndex(_inode, input.getInode()), input.getStat());
        }
    }

    private List<DirectoryEntry> listPseudoDirectory(Inode parent) throws ChimeraNFSException, IOException {
        Set<PseudoFsNode> nodes = prepareExportTree();
        for (PseudoFsNode node : nodes) {
            if (node.id().equals(parent)) {
                if (node.isMountPoint()) {
                    return Lists.transform(_inner.list(parent), new ConvertToRealInode(node));
                } else {
                    List<DirectoryEntry> pseudoLs = new ArrayList<DirectoryEntry>();
                    for (String s : node.getChildren()) {
                        PseudoFsNode subNode = node.getChild(s);
                        Inode inode = subNode.id();
                        Stat stat = _inner.getattr(inode);
                        DirectoryEntry e = new DirectoryEntry(s,
                                subNode.isMountPoint()
                                ? pseudoIdToReal(inode, getIndexId(subNode)) : inode, stat);
                        pseudoLs.add(e);
                    }
                    return pseudoLs;
                }
            }
        }
        throw new ChimeraNFSException(nfsstat.NFSERR_NOENT, "");
    }

    private Inode pushExportIndex(Inode parent, Inode inode) {

        FileHandle fh = new FileHandle.FileHandleBuilder()
                .setExportIdx(parent.exportIndex())
                .setType(0)
                .build(inode.getFileId());
        return new Inode(fh);
    }

    private Inode realToPseudo(Inode inode) {
        return realToPseudo(inode, 0);
    }

    private Inode realToPseudo(Inode inode, int idx) {

        FileHandle fh = new FileHandle.FileHandleBuilder()
                .setExportIdx(idx)
                .setType(1)
                .build(inode.getFileId());
        return new Inode(fh);
    }

    private void pathToPseudoFs(final PseudoFsNode root, Set<PseudoFsNode> all, FsExport e) throws IOException {

        PseudoFsNode parent = root;
        String path = e.getPath();

        if (e.getPath().equals("/")) {
            root.addExport(e);
            return;
        }

        Splitter splitter = Splitter.on('/').omitEmptyStrings();
        Set<PseudoFsNode> pathNodes = new HashSet<PseudoFsNode>();

        for (String s : splitter.split(path)) {
            try {
                PseudoFsNode node = parent.getChild(s);
                if (node == null) {
                    node = new PseudoFsNode(realToPseudo(_inner.lookup(parent.id(), s)));
                    parent.addChild(s, node);
                    pathNodes.add(node);
                }
                parent = node;
            } catch (IOException ef) {
                return;
            }
        }

        all.addAll(pathNodes);
        parent.setId(pseudoIdToReal(parent.id(), e.getIndex()));
        parent.addExport(e);
    }

    private Set<PseudoFsNode> prepareExportTree() throws ChimeraNFSException, IOException {

        Collection<FsExport> exports = _exportFile.exportsFor(_inetAddress);
        if (exports.isEmpty()) {
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "");
        }

        Set<PseudoFsNode> nodes = new HashSet<PseudoFsNode>();
        Inode rootInode = realToPseudo(_inner.getRootInode());
        PseudoFsNode root = new PseudoFsNode(rootInode);

        for (FsExport export : exports) {
            pathToPseudoFs(root, nodes, export);
        }

        if (nodes.isEmpty()) {
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "");
        }

        nodes.add(root);
        return nodes;
    }
}
