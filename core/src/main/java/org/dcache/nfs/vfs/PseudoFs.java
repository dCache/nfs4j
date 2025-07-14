/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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

import static com.google.common.collect.Lists.newArrayList;
import static org.dcache.nfs.util.UnixSubjects.hasGid;
import static org.dcache.nfs.util.UnixSubjects.hasUid;
import static org.dcache.nfs.util.UnixSubjects.isNobodySubject;
import static org.dcache.nfs.util.UnixSubjects.isRootSubject;
import static org.dcache.nfs.util.UnixSubjects.toSubject;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_DELETE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_EXECUTE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_EXTEND;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_LOOKUP;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_MODIFY;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_READ;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_XALIST;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_XAREAD;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACCESS4_XAWRITE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_ADD_FILE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_ADD_SUBDIRECTORY;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_APPEND_DATA;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_DELETE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_DELETE_CHILD;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_EXECUTE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_LIST_DIRECTORY;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_READ_ACL;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_READ_ATTRIBUTES;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_READ_DATA;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_WRITE_ACL;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_WRITE_ATTRIBUTES;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_WRITE_DATA;
import static org.dcache.nfs.v4.xdr.nfs4_prot.ACE4_WRITE_OWNER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.security.auth.Subject;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.ExportTable;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.PermException;
import org.dcache.nfs.status.RoFsException;
import org.dcache.nfs.util.Opaque;
import org.dcache.nfs.util.SubjectHolder;
import org.dcache.nfs.v4.NFS4Client;
import org.dcache.nfs.v4.NFSv4StateHandler;
import org.dcache.nfs.v4.acl.Acls;
import org.dcache.nfs.v4.xdr.acemask4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.AclCheckable.Access;
import org.dcache.nfs.vfs.Stat.StatAttribute;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcAuthType;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.gss.RpcAuthGss;
import org.dcache.oncrpc4j.rpc.gss.RpcGssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

/**
 * A decorated {@code VirtualFileSystem} that builds a Pseudo file system on top of an other file system based on export
 * rules.
 *
 * In addition, PseudoFS takes the responsibility of permission and access checking.
 */
public class PseudoFs extends ForwardingFileSystem {

    private final static Logger _log = LoggerFactory.getLogger(PseudoFs.class);

    /** TCP port range between 0 and 1023 can be used only by privileged (root) user */
    public static final int PRIVILEGED_PORT = 1023;

    private final Subject _subject;
    private final InetSocketAddress _inetAddress;
    private final VirtualFileSystem _inner;
    private final ExportTable _exportTable;
    private final RpcAuth _auth;
    private final NFSv4StateHandler _stateHandler;

    private final static int ACCESS4_MASK =
            ACCESS4_DELETE | ACCESS4_EXECUTE | ACCESS4_EXTEND
                    | ACCESS4_LOOKUP | ACCESS4_MODIFY | ACCESS4_READ
                    | ACCESS4_XAREAD | ACCESS4_XAWRITE | ACCESS4_XALIST;

    public PseudoFs(VirtualFileSystem inner, RpcCall call, ExportTable exportTable) {
        this(inner, call, exportTable, null);
    }

    public PseudoFs(VirtualFileSystem inner, RpcCall call, ExportTable exportTable, NFSv4StateHandler stateHandler) {
        _inner = inner;
        _subject = call.getCredential().getSubject();
        _auth = call.getCredential();
        _inetAddress = call.getTransport().getRemoteSocketAddress();
        _exportTable = exportTable;
        _stateHandler = stateHandler;
    }

    @Override
    protected VirtualFileSystem delegate() {
        return _inner;
    }

    private boolean canAccess(Inode inode, Stat stat, int mode) {
        try {
            checkAccess(inode, stat, mode, false);
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    @Override
    public int access(Subject subject, Inode inode, int mode) throws IOException {
        int accessmask = 0;

        if ((mode & ~ACCESS4_MASK) != 0) {
            throw new InvalException("invalid access mask");
        }

        Stat stat = _inner.getattr(innerInode(inode));
        if ((mode & ACCESS4_READ) != 0) {
            if (canAccess(inode, stat, ACE4_READ_DATA)) {
                accessmask |= ACCESS4_READ;
            }
        }

        if ((mode & ACCESS4_LOOKUP) != 0) {
            if (canAccess(inode, stat, ACE4_EXECUTE)) {
                accessmask |= ACCESS4_LOOKUP;
            }
        }

        if ((mode & ACCESS4_MODIFY) != 0) {
            if (canAccess(inode, stat, ACE4_WRITE_DATA)) {
                accessmask |= ACCESS4_MODIFY;
            }
        }

        if ((mode & ACCESS4_EXECUTE) != 0) {
            if (canAccess(inode, stat, ACE4_EXECUTE)) {
                accessmask |= ACCESS4_EXECUTE;
            }
        }

        if ((mode & ACCESS4_EXTEND) != 0) {
            if (canAccess(inode, stat, ACE4_APPEND_DATA)) {
                accessmask |= ACCESS4_EXTEND;
            }
        }

        if ((mode & ACCESS4_DELETE) != 0) {
            if (canAccess(inode, stat, ACE4_DELETE_CHILD)) {
                accessmask |= ACCESS4_DELETE;
            }
        }

        /**
         * rfc8276 specifies only 'user' attributes. Thus access to access to them is controlled as access to the file:
         * - to read or list xattrs file read permission is required - to set or delete xattrs file write permission is
         * required
         */

        if ((mode & ACCESS4_XAREAD) != 0) {
            if (canAccess(inode, stat, ACE4_READ_DATA)) {
                accessmask |= ACCESS4_XAREAD;
            }
        }

        if ((mode & ACCESS4_XALIST) != 0) {
            if (canAccess(inode, stat, ACE4_READ_DATA)) {
                accessmask |= ACCESS4_XALIST;
            }
        }

        if ((mode & ACCESS4_XAWRITE) != 0) {
            if (canAccess(inode, stat, ACE4_WRITE_DATA)) {
                accessmask |= ACCESS4_XAWRITE;
            }
        }

        return accessmask & _inner.access(subject, innerInode(inode), accessmask);
    }

    @Override
    public Inode create(Inode parent, Stat.Type type, String path, Subject subject, int mode) throws IOException {
        Subject effectiveSubject = checkAccess(parent, ACE4_ADD_FILE);

        if (subject != null && isRootSubject(effectiveSubject)) {
            effectiveSubject = subject;
        }

        Inode innerParent = innerInode(parent);

        if (inheritUidGid(parent)) {
            Stat s = _inner.getattr(innerParent);
            effectiveSubject = toSubject(s.getUid(), s.getGid());
        }

        return pushExportIndex(parent, _inner.create(innerParent, type, path, effectiveSubject, mode));
    }

    @Override
    public Inode getRootInode() throws IOException {
        /*
         * reject if there are no exports for this client at all
         */
        if (!_exportTable.exports(_inetAddress.getAddress()).findAny().isPresent()) {
            _log.warn("Access denied: (no export) fs root for client {}", _inetAddress);
            throw new AccessException("no exports");
        }

        Inode inode = _inner.getRootInode();
        FsExport export = _exportTable.getExport("/", _inetAddress.getAddress());
        return export == null ? realToPseudo(inode) : pushExportIndex(inode, export.getIndex());
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        checkAccess(parent, ACE4_EXECUTE);

        if (parent.isPseudoInode()) {
            return lookupInPseudoDirectory(parent, path);
        }

        /*
         * REVISIT: this is not the best place to do it, but the simples one.
         */
        FsExport export = _exportTable.getExport(parent.exportIndex(), _inetAddress.getAddress());
        if (!export.isWithDcap() && ".(get)(cursor)".equals(path)) {
            throw new NoEntException("the dcap magic file is blocked");
        }

        return pushExportIndex(parent, _inner.lookup(innerInode(parent), path));
    }

    @Override
    public Inode link(Inode parent, Inode link, String path, Subject subject) throws IOException {
        checkAccess(link, ACE4_WRITE_ATTRIBUTES);
        Subject effectiveSubject = checkAccess(parent, ACE4_ADD_FILE);

        Inode innerParent = innerInode(parent);
        if (inheritUidGid(parent)) {
            Stat s = _inner.getattr(innerParent);
            effectiveSubject = toSubject(s.getUid(), s.getGid());
        }
        return pushExportIndex(parent, _inner.link(innerParent, link, path, effectiveSubject));
    }

    @Override
    public DirectoryStream list(Inode inode, byte[] verifier, long cookie) throws IOException {
        Subject effectiveSubject = checkAccess(inode, ACE4_LIST_DIRECTORY);
        if (inode.isPseudoInode()) {
            return new DirectoryStream(listPseudoDirectory(inode)).tail(cookie);
        }
        DirectoryStream innerStrem = _inner.list(innerInode(inode), verifier, cookie);
        return innerStrem.transform(new PushParentIndex(inode));
    }

    @Override
    public Inode mkdir(Inode parent, String path, Subject subject, int mode) throws IOException {
        Subject effectiveSubject = checkAccess(parent, ACE4_ADD_SUBDIRECTORY);
        if (subject != null && isRootSubject(effectiveSubject)) {
            effectiveSubject = subject;
        }

        Inode innerParent = innerInode(parent);
        if (inheritUidGid(parent)) {
            Stat s = _inner.getattr(innerParent);
            effectiveSubject = toSubject(s.getUid(), s.getGid());
        }
        return pushExportIndex(parent, _inner.mkdir(innerParent, path, effectiveSubject, mode));
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        checkAccess(src, ACE4_DELETE_CHILD);
        checkAccess(dest, ACE4_ADD_FILE | ACE4_DELETE_CHILD);
        return _inner.move(innerInode(src), oldName, innerInode(dest), newName);
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {

        Inode parent = _inner.parentOf(innerInode(inode));
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
        return _inner.read(innerInode(inode), data, offset, count);
    }

    @Override
    public int read(Inode inode, ByteBuffer data, long offset) throws IOException {
        checkAccess(inode, ACE4_READ_DATA);
        return _inner.read(innerInode(inode), data, offset);
    }

    private void checkAccessReadWriteData(Opaque stateId, Inode inode, boolean write) throws IOException {
        if (_stateHandler != null) {
            NFS4Client client = _stateHandler.getClientIfExists(stateid4.getClientId(stateId));
            if (client != null) {
                int shareAccess;
                try {
                    shareAccess = _stateHandler.getFileTracker().getShareAccess(client, inode, stateId);
                    if ((shareAccess & (write ? nfs4_prot.OPEN4_SHARE_ACCESS_WRITE
                            : nfs4_prot.OPEN4_SHARE_ACCESS_READ)) != 0) {
                        return; // permit
                    }
                } catch (ChimeraNFSException e) {
                    // ignore; check below and throw proper exception
                }
            }
        }
        checkAccess(inode, write ? ACE4_WRITE_DATA : ACE4_READ_DATA);
    }

    @Override
    public int read(Opaque stateId, Inode inode, ByteBuffer data, long offset, Runnable eofReached) throws IOException {
        checkAccessReadWriteData(stateId, inode, false);
        return _inner.read(stateId, innerInode(inode), data, offset, eofReached);
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_DATA);
        return _inner.readlink(innerInode(inode));
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
        Inode innerParent = innerInode(parent);
        try {
            checkAccess(parent, ACE4_DELETE_CHILD);
        } catch (ChimeraNFSException e) {
            if (e.getStatus() == nfsstat.NFSERR_ACCESS) {
                Inode inode = pushExportIndex(parent, _inner.lookup(innerParent, path));
                checkAccess(inode, ACE4_DELETE);
            } else {
                throw e;
            }
        }
        _inner.remove(innerParent, path);
    }

    @Override
    public Inode symlink(Inode parent, String path, String link, Subject subject, int mode) throws IOException {
        Inode innerParent = innerInode(parent);

        Subject effectiveSubject = checkAccess(parent, ACE4_ADD_FILE);
        if (inheritUidGid(parent)) {
            Stat s = _inner.getattr(innerParent);
            effectiveSubject = toSubject(s.getUid(), s.getGid());
        }
        return pushExportIndex(parent, _inner.symlink(innerParent, path, link, effectiveSubject, mode));
    }

    @Override
    public WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel)
            throws IOException {
        checkAccess(inode, ACE4_WRITE_DATA);
        return _inner.write(innerInode(inode), data, offset, count, stabilityLevel);
    }

    @Override
    public WriteResult write(Inode inode, ByteBuffer data, long offset, StabilityLevel stabilityLevel)
            throws IOException {
        checkAccess(inode, ACE4_WRITE_DATA);
        return _inner.write(innerInode(inode), data, offset, stabilityLevel);
    }

    @Override
    public WriteResult write(Opaque stateId, Inode inode, ByteBuffer data, long offset, StabilityLevel stabilityLevel)
            throws IOException {
        checkAccessReadWriteData(stateId, inode, true);
        return _inner.write(stateId, innerInode(inode), data, offset, stabilityLevel);
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_ATTRIBUTES);
        return _inner.getattr(innerInode(inode));
    }

    @Override
    public Stat getattr(Inode inode, EnumSet<StatAttribute> attributes) throws IOException {
        checkAccess(inode, ACE4_READ_ATTRIBUTES);
        return _inner.getattr(innerInode(inode), attributes);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        int mask = ACE4_WRITE_ATTRIBUTES;
        if (stat.isDefined(Stat.StatAttribute.OWNER)) {
            /*
             *
             * According POSIX changing of owner_group for non privileged process if owner is equal to the file's user
             * ID or (uid_t)-1. (See: http://pubs.opengroup.org/onlinepubs/9699919799/functions/chown.html)
             *
             * As we already enforce WRITE_ATTRIBUTES, e.g. file's owner matching subjects, remove required WRITE_OWNER
             * only if new owner is different.
             */
            int currentOwner = getattr(inode).getUid();
            if (currentOwner == stat.getUid() || stat.getUid() == -1) {
                stat.undefine(Stat.StatAttribute.OWNER);
            } else {
                mask |= ACE4_WRITE_OWNER;
            }
        }

        if (stat.isDefined(Stat.StatAttribute.SIZE)) {
            mask |= ACE4_WRITE_DATA | ACE4_APPEND_DATA;
        }

        checkAccess(inode, mask);
        _inner.setattr(innerInode(inode), stat);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_ACL);
        return _inner.getAcl(innerInode(inode));
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        checkAccess(inode, ACE4_WRITE_ACL);
        _inner.setAcl(innerInode(inode), acl);
    }

    @Override
    public byte[] getXattr(Inode inode, String attr) throws IOException {
        checkAccess(inode, ACE4_READ_DATA);
        return _inner.getXattr(innerInode(inode), attr);
    }

    @Override
    public void setXattr(Inode inode, String attr, byte[] value, SetXattrMode mode) throws IOException {
        checkAccess(inode, ACE4_WRITE_DATA);
        _inner.setXattr(innerInode(inode), attr, value, mode);
    }

    @Override
    public String[] listXattrs(Inode inode) throws IOException {
        checkAccess(inode, ACE4_READ_DATA);
        return _inner.listXattrs(innerInode(inode));
    }

    @Override
    public void removeXattr(Inode inode, String attr) throws IOException {
        checkAccess(inode, ACE4_WRITE_DATA);
        _inner.removeXattr(innerInode(inode), attr);
    }

    private Subject checkAccess(Inode inode, int requestedMask) throws IOException {
        return checkAccess(inode, requestedMask, true);
    }

    private Subject checkAccess(Inode inode, int requestedMask, boolean shouldLog) throws IOException {
        Stat stat;
        if (requestedMask == ACE4_READ_ATTRIBUTES) {
            stat = null; // not required
        } else {
            stat = _inner.getattr(innerInode(inode));
        }

        return checkAccess(inode, stat, requestedMask, shouldLog);
    }

    private Subject checkAccess(Inode inode, Stat stat, int requestedMask, boolean shouldLog) throws IOException {

        Subject effectiveSubject = _subject;
        Access aclMatched = Access.UNDEFINED;

        if (inode.isPseudoInode() && Acls.wantModify(requestedMask)) {
            if (shouldLog) {
                _log.warn("Access denied: pseudo Inode {} {} {} {}",
                        inode, _inetAddress,
                        acemask4.toString(requestedMask),
                        new SubjectHolder(effectiveSubject));
            }
            throw new RoFsException("attempt to modify pseudofs");
        }

        if (!inode.isPseudoInode()) {
            int exportIdx = getExportIndex(inode);
            FsExport export = _exportTable.getExport(exportIdx, _inetAddress.getAddress());
            if (exportIdx != 0 && export == null) {
                if (shouldLog) {
                    _log.warn("Access denied: (no export) to inode {} for client {}", inode, _inetAddress);
                }
                throw new AccessException("permission deny");
            }

            if (export.isPrivilegedClientPortRequired() && _inetAddress.getPort() > PRIVILEGED_PORT) {
                if (shouldLog) {
                    _log.warn("Access denied: unprivileged client {}", _inetAddress);
                }
                throw new AccessException("unprivileged client");
            }
            checkSecurityFlavor(_auth, export.getSec());

            if ((export.ioMode() == FsExport.IO.RO) && Acls.wantModify(requestedMask)) {
                if (shouldLog) {
                    _log.warn("Access denied: (RO export) inode {} for client {}", inode, _inetAddress);
                }
                throw new AccessException("read-only export");
            }

            if (export.isAllRoot()) {
                _log.debug("permission check to inode {} skipped due to all_root option for client {}",
                        inode, _inetAddress);
                return effectiveSubject;
            }

            if (isNobodySubject(_subject) || export.hasAllSquash() || (!export.isTrusted() && isRootSubject(
                    _subject))) {
                effectiveSubject = toSubject(export.getAnonUid(), export.getAnonGid());
            }

            if (export.checkAcls()) {
                aclMatched = _inner.getAclCheckable().checkAcl(_subject, innerInode(inode), requestedMask);
                if (aclMatched == Access.DENY) {
                    if (shouldLog) {
                        _log.warn("Access deny: {} {} {}", _inetAddress, acemask4.toString(requestedMask),
                                new SubjectHolder(_subject));
                    }
                    throw new AccessException();
                }
            }
        }

        /*
         * check for unix permission if ACL did not give us an answer. Skip the check, if we ask for
         * ACE4_READ_ATTRIBUTES as unix always allows it.
         */
        if ((aclMatched == Access.UNDEFINED) && (requestedMask != ACE4_READ_ATTRIBUTES)) {
            int unixAccessmask = unixToAccessmask(effectiveSubject, stat);
            if ((unixAccessmask & requestedMask) != requestedMask) {
                if (shouldLog) {
                    _log.warn("Access denied: {} {} {} {} {}", inode, _inetAddress,
                            acemask4.toString(requestedMask),
                            acemask4.toString(unixAccessmask), new SubjectHolder(_subject));
                }
                throw new AccessException("permission deny");
            }
        }
        return effectiveSubject;
    }

    /*
     * unix permission bits offset as defined in POSIX for st_mode filed of the stat structure.
     */
    private static final int BIT_MASK_OWNER_OFFSET = 6;
    private static final int BIT_MASK_GROUP_OFFSET = 3;
    private static final int BIT_MASK_OTHER_OFFSET = 0;

    @SuppressWarnings("PointlessBitwiseExpression")
    private int unixToAccessmask(Subject subject, Stat stat) {
        int mode = stat.getMode();
        boolean isDir = (mode & Stat.S_IFDIR) == Stat.S_IFDIR;
        int fromUnixMask;

        if (isRootSubject(subject)) {
            fromUnixMask = Acls.toAccessMask(Acls.RBIT | Acls.WBIT | Acls.XBIT, isDir, true);
            fromUnixMask |= ACE4_WRITE_OWNER;
        } else if (hasUid(subject, stat.getUid())) {
            fromUnixMask = Acls.toAccessMask(mode >> BIT_MASK_OWNER_OFFSET, isDir, true);
        } else if (hasGid(subject, stat.getGid())) {
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
        throw new NoEntException();
    }

    private boolean isPseudoDirectory(Inode dir) throws IOException {
        return prepareExportTree().stream()
                .anyMatch(n -> n.id().equals(dir));
    }

    public static Inode pseudoIdToReal(Inode inode, int index) {
        return new Inode(0, index, 0, inode.getFileId());
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
                    input.getStat(), input.getCookie());
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
                    pushExportIndex(_inode, input.getInode()), input.getStat(), input.getCookie());
        }
    }

    private Collection<DirectoryEntry> listPseudoDirectory(Inode parent) throws ChimeraNFSException, IOException {
        Set<PseudoFsNode> nodes = prepareExportTree();
        for (PseudoFsNode node : nodes) {
            if (node.id().equals(parent)) {
                if (node.isMountPoint()) {
                    return newArrayList(_inner.list(innerInode(parent), null, 0L).transform(new ConvertToRealInode(
                            node)));
                } else {
                    long cookie = 3; // artificial cookie. Values 0, 1 and 2 are reserved.
                    List<DirectoryEntry> pseudoLs = new ArrayList<>();
                    for (String s : node.getChildren()) {
                        PseudoFsNode subNode = node.getChild(s);
                        Inode inode = subNode.id();
                        Stat stat = _inner.getattr(innerInode(inode));
                        DirectoryEntry e = new DirectoryEntry(s,
                                subNode.isMountPoint()
                                        ? pseudoIdToReal(inode, getIndexId(subNode)) : inode, stat, cookie);
                        pseudoLs.add(e);
                        cookie++;
                    }
                    return pseudoLs;
                }
            }
        }
        throw new NoEntException();
    }

    private Inode pushExportIndex(Inode inode, int index) {
        return pseudoIdToReal(inode, index);
    }

    private Inode pushExportIndex(Inode parent, Inode inode) {
        return pushExportIndex(inode, getExportIndex(parent));
    }

    private int getExportIndex(Inode inode) {
        /*
         * NOTE, we take first export entry allowed for this client. This can be wrong, e.g. RO vs. RW.
         */
        if (inode.handleVersion() == 0) {
            FsExport export = _exportTable.exports(_inetAddress.getAddress())
                    .findFirst()
                    .orElse(null);
            return export == null ? -1 : export.getIndex();
        }
        return inode.exportIndex();
    }

    private Inode realToPseudo(Inode inode) {
        return realToPseudo(inode, 0);
    }

    private Inode realToPseudo(Inode inode, int index) {
        return new Inode(0, index, 1, inode.getFileId());
    }

    private void pathToPseudoFs(final PseudoFsNode root, Set<PseudoFsNode> all, FsExport e) {

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
                    node = new PseudoFsNode(realToPseudo(_inner.lookup(innerInode(parent.id()), s)));
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

        Set<PseudoFsNode> nodes = new HashSet<>();
        Inode rootInode = realToPseudo(_inner.getRootInode());
        PseudoFsNode root = new PseudoFsNode(rootInode);

        _exportTable.exports(_inetAddress.getAddress()).forEach(e -> pathToPseudoFs(root, nodes, e));

        if (nodes.isEmpty()) {
            _log.warn("No exports found for: {}", _inetAddress);
            throw new AccessException();
        }

        nodes.add(root);
        return nodes;
    }

    private static void checkSecurityFlavor(RpcAuth auth, FsExport.Sec minFlavor) throws ChimeraNFSException {

        FsExport.Sec usedFlavor;
        switch (auth.type()) {
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
                        throw new PermException("Unsupported Authentication GSS service: " + authGss.getService());
                }
                break;
            default:
                throw new PermException("Unsupported Authentication flavor: " + auth.type());
        }

        if (usedFlavor.compareTo(minFlavor) < 0) {
            throw new PermException("Authentication flavor too weak: "
                    + "allowed <" + minFlavor + "> provided <" + usedFlavor + ">");
        }
    }

    private boolean inheritUidGid(Inode inode) {
        return _exportTable.getExport(inode.exportIndex(), _inetAddress.getAddress()).isAllRoot();
    }

    /**
     * Convert an {@link Inode} that is used by {@link PseudoFs} to an {@link Inode} that is understood
     * by the underlying file system.
     * <p>
     * We currently store additional information such as "ExportId" and "PseudoInode" as parts of the Inode.
     * {@link VirtualFileSystem}s that store {@link Inode} as a whole (rather than only the "fileId" bit)
     * may not recognize such objects, unless we remove this additional information.
     * <p>
     * Once {@link PseudoFs} handles this configuration internally, we can remove this conversion step.
     *
     * @param inode The {@link Inode} as passed from and to the NFS client.
     * @return The {@link Inode} as passed from {@link PseudoFs} to the underlying {@link VirtualFileSystem}.
     * @throws IOException on error.
     */
    private Inode innerInode(Inode inode) throws IOException {
        if (inode.isPseudoInode()) {
            Inode innerRootInode = _inner.getRootInode();
            if (innerRootInode.getFileIdKey().equals(inode.getFileIdKey())) {
                return innerRootInode;
            }
        }
        return Inode.innerInode(inode);
    }
}
