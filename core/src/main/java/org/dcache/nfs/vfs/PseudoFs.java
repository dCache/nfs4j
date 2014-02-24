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
package org.dcache.nfs.vfs;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import org.dcache.auth.Subjects;
import org.dcache.chimera.UnixPermission;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.NfsUser;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.acl.Acls;
import org.dcache.nfs.v4.xdr.acemask4;
import org.dcache.xdr.RpcCall;
import static org.dcache.nfs.v4.xdr.nfs4_prot.*;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.gss.RpcAuthGss;
import org.dcache.xdr.gss.RpcGssService;
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
    private final RpcAuth _auth;

    public PseudoFs(VirtualFileSystem inner, RpcCall call, ExportFile exportFile) {
        _inner = inner;
        _subject = call.getCredential().getSubject();
        _auth = call.getCredential();
        _inetAddress = call.getTransport().getRemoteSocketAddress().getAddress();
        _exportFile = exportFile;
    }

    private boolean canAccess(Inode inode, int mode) {
        try {
            checkAccess(inode, mode, false);
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        int accessmask = 0;

        if ((mode & ACCESS4_READ) != 0) {
            if (canAccess(inode, ACE4_READ_DATA)) {
                accessmask |= ACCESS4_READ;
            }
        }

        if ((mode & ACCESS4_LOOKUP) != 0) {
            if (canAccess(inode, ACE4_EXECUTE)) {
                accessmask |= ACCESS4_LOOKUP;
            }
        }

        if ((mode & ACCESS4_MODIFY) != 0) {
            if (canAccess(inode, ACE4_WRITE_DATA)) {
                accessmask |= ACCESS4_MODIFY;
            }
        }

        if ((mode & ACCESS4_EXECUTE) != 0) {
            if (canAccess(inode, ACE4_EXECUTE)) {
                accessmask |= ACCESS4_EXECUTE;
            }
        }

        if ((mode & ACCESS4_EXTEND) != 0) {
            if (canAccess(inode, ACE4_APPEND_DATA)) {
                accessmask |= ACCESS4_EXTEND;
            }
        }

        if ((mode & ACCESS4_DELETE) != 0) {
            if (canAccess(inode, ACE4_DELETE_CHILD)) {
                accessmask |= ACCESS4_DELETE;
            }
        }

        return accessmask & _inner.access(inode, accessmask);
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
        /*
         * reject if there are no exports for this client at all
         */
        if (Iterables.isEmpty(_exportFile.exportsFor(_inetAddress))) {
            _log.warn("Access denied: (no export) fs root for client {}", _inetAddress);
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "no exports");
        }

        Inode inode = _inner.getRootInode();
        FsExport export = _exportFile.getExport("/", _inetAddress);
        return export == null? realToPseudo(inode) :
                pushExportIndex(inode, export.getIndex());
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        checkAccess(parent, ACE4_EXECUTE);

        if (parent.isPesudoInode()) {
            return lookupInPseudoDirectory(parent, path);
        }

	/*
	 * REVISIT: this is not the best place to do it, but the simples one.
	 */
	FsExport export = _exportFile.getExport(parent.exportIndex(), _inetAddress);
	if (!export.isWithDcap() && ".(get)(cursor)".equals(path)) {
	    throw new ChimeraNFSException(nfsstat.NFSERR_NOENT, "the dcap magic file is blocked");
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
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        checkAccess(src, ACE4_DELETE_CHILD);
        checkAccess(dest, ACE4_ADD_FILE | ACE4_DELETE_CHILD);
        return _inner.move(src, oldName, dest, newName);
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {

	Inode parent = _inner.parentOf(inode);
	Inode asPseudo = realToPseudo(parent);
	if (isPseudoDirectory(asPseudo)) {
	    /*
	     * if parent is a path of export tree
	     */
	    return asPseudo;
	} else {
	    return pushExportIndex(inode, parent);
	}
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
    public void remove(Inode parent, String path) throws IOException {
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
        _inner.remove(parent, path);
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

    @Override
    public boolean hasIOLayout(Inode inode) throws IOException {
        return _inner.hasIOLayout(inode);
    }

    private void checkAccess(Inode inode, int requestedMask) throws IOException {
        checkAccess(inode, requestedMask, true);
    }

    private void checkAccess(Inode inode, int requestedMask, boolean shouldLog) throws IOException {

        Subject effectiveSubject = _subject;
        Stat stat = _inner.getattr(inode);
        boolean aclMatched = false;

        if (inode.isPesudoInode() && Acls.wantModify(requestedMask)) {
            if (shouldLog) {
                _log.warn("Access denied: pseudo Inode {} {} {}",
                            inode, acemask4.toString(requestedMask),
                            effectiveSubject);
            }
            throw new ChimeraNFSException(nfsstat.NFSERR_ROFS, "attempt to modify pseudofs");
        }

        if (!inode.isPesudoInode()) {
            int exportIdx = getExportIndex(inode);
            FsExport export = _exportFile.getExport(exportIdx, _inetAddress);
            if (exportIdx != 0 && export == null) {
                if (shouldLog) {
                    _log.warn("Access denied: (no export) to inode {} for client {}", inode, _inetAddress);
                }
                throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "permission deny");
            }

            checkSecurityFlavor(_auth, export.getSec());

            if ( (export.ioMode() == FsExport.IO.RO) && Acls.wantModify(requestedMask)) {
                if (shouldLog) {
                    _log.warn("Access denied: (RO export) inode {} for client {}", inode, _inetAddress);
                }
                throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "read-only export");
            }

            if (export.hasAllSquash() || (!export.isTrusted() && Subjects.isRoot(_subject))) {
                effectiveSubject = NfsUser.NFS_NOBODY;
            }

            if (export.checkAcls()) {
                ChimeraVfs chimeraVfs = (ChimeraVfs) _inner;
                aclMatched = chimeraVfs.checkAclAccess(_subject, inode, requestedMask);
            }
        }

        if (!aclMatched) {
            int unixAccessmask = unixToAccessmask(effectiveSubject, stat);
            if ((unixAccessmask & requestedMask) != requestedMask) {
                if (shouldLog) {
                    _log.warn("Access denied: {} {} {} {}", inode,
                                acemask4.toString(requestedMask),
                                acemask4.toString(unixAccessmask), _subject);
                }
                throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "permission deny");
            }
        }
    }

    /*
     * unix permission bits offset as defined in POSIX
     * for st_mode filed of the stat  structure.
     */
    private static final int BIT_MASK_OWNER_OFFSET = 6;
    private static final int BIT_MASK_GROUP_OFFSET = 3;
    private static final int BIT_MASK_OTHER_OFFSET = 0;

    @SuppressWarnings("PointlessBitwiseExpression")
    private int unixToAccessmask(Subject subject, Stat stat) {
        int mode = stat.getMode();
        boolean isDir = (mode & UnixPermission.S_IFDIR) == UnixPermission.S_IFDIR;
        int fromUnixMask;

        if (Subjects.isRoot(subject)) {
            fromUnixMask = Acls.toAccessMask(Acls.RBIT | Acls.WBIT | Acls.XBIT, isDir, true);
        } else if (Subjects.hasUid(subject, stat.getUid())) {
            fromUnixMask = Acls.toAccessMask(mode >> BIT_MASK_OWNER_OFFSET, isDir, true);
        } else if (Subjects.hasGid(subject, stat.getGid())) {
            fromUnixMask = Acls.toAccessMask(mode >> BIT_MASK_GROUP_OFFSET, isDir, false);
        } else {
            fromUnixMask = Acls.toAccessMask(mode >> BIT_MASK_OTHER_OFFSET, isDir, false);
        }
        return fromUnixMask;
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

    private boolean isPseudoDirectory(Inode dir) throws IOException {
	Set<PseudoFsNode> nodes = prepareExportTree();

	for (PseudoFsNode node : nodes) {
	    if (node.id().equals(dir)) {
		return true;
	    }
	}
	return false;
    }

    public static Inode pseudoIdToReal(Inode inode, int index) {

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
                    List<DirectoryEntry> pseudoLs = new ArrayList<>();
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

    private Inode pushExportIndex(Inode inode, int index) {

        FileHandle fh = new FileHandle.FileHandleBuilder()
                .setExportIdx(index)
                .setType(0)
                .build(inode.getFileId());
        return new Inode(fh);
    }

    private Inode pushExportIndex(Inode parent, Inode inode) {
        return pushExportIndex(inode, getExportIndex(parent));
    }

    private int getExportIndex(Inode inode) {
        /*
         * NOTE, we take first export entry allowed for this client.
         * This can be wrong, e.g. RO vs. RW.
         */
        if (inode.handleVersion() == 0) {
            FsExport export = Iterables.getFirst(_exportFile.exportsFor(_inetAddress), null);
            return export == null? -1 : export.getIndex();
        }
        return inode.exportIndex();
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
        Set<PseudoFsNode> pathNodes = new HashSet<>();

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

        Iterable<FsExport> exports = _exportFile.exportsFor(_inetAddress);

        Set<PseudoFsNode> nodes = new HashSet<>();
        Inode rootInode = realToPseudo(_inner.getRootInode());
        PseudoFsNode root = new PseudoFsNode(rootInode);

        for (FsExport export : exports) {
            pathToPseudoFs(root, nodes, export);
        }

        if (nodes.isEmpty()) {
            _log.warn("No exports found for: {}", _inetAddress);
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "");
        }

        nodes.add(root);
        return nodes;
    }

    private static void checkSecurityFlavor(RpcAuth auth, FsExport.Sec minFlavor) throws ChimeraNFSException {

        FsExport.Sec usedFlavor;
        switch(auth.type()) {
            case RpcAuthType.NONE:
                usedFlavor = FsExport.Sec.NONE;
                break;
            case RpcAuthType.UNIX:
                usedFlavor = FsExport.Sec.SYS;
                break;
            case RpcAuthType.RPCGSS_SEC:
                RpcAuthGss authGss = (RpcAuthGss) auth;
                switch (authGss.getService()) {
                    case RpcGssService.RPC_GSS_SVC_NONE:
                        usedFlavor = FsExport.Sec.KRB5;
                        break;
                    case RpcGssService.RPC_GSS_SVC_INTEGRITY:
                        usedFlavor = FsExport.Sec.KRB5I;
                        break;
                    case RpcGssService.RPC_GSS_SVC_PRIVACY:
                        usedFlavor = FsExport.Sec.KRB5P;
                        break;
                    default:
                        throw new ChimeraNFSException(nfsstat.NFSERR_PERM,
                                "Unsupported Authentication GSS service: " + authGss.getService());
                }
                break;
            default:
                throw new ChimeraNFSException(nfsstat.NFSERR_PERM,
                        "Unsupported Authentication flavor: " + auth.type());
        }

        if (usedFlavor.compareTo(minFlavor) < 0) {
            throw new ChimeraNFSException(nfsstat.NFSERR_PERM,
                        "Authentication flavor too weak: "
                    + "allowed <" + minFlavor + "> provided <" + usedFlavor + ">");
        }
    }
}
