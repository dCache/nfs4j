/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v3;

import org.dcache.auth.Subjects;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v3.xdr.LOOKUP3res;
import org.dcache.nfs.v3.xdr.WRITE3resfail;
import org.dcache.nfs.v3.xdr.RMDIR3resok;
import org.dcache.nfs.v3.xdr.SYMLINK3resfail;
import org.dcache.nfs.v3.xdr.post_op_fh3;
import org.dcache.nfs.v3.xdr.READLINK3args;
import org.dcache.nfs.v3.xdr.uint64;
import org.dcache.nfs.v3.xdr.MKDIR3res;
import org.dcache.nfs.v3.xdr.WRITE3args;
import org.dcache.nfs.v3.xdr.createmode3;
import org.dcache.nfs.v3.xdr.post_op_attr;
import org.dcache.nfs.v3.xdr.LINK3resfail;
import org.dcache.nfs.v3.xdr.READ3resfail;
import org.dcache.nfs.v3.xdr.MKDIR3resok;
import org.dcache.nfs.v3.xdr.READDIR3args;
import org.dcache.nfs.v3.xdr.LOOKUP3resfail;
import org.dcache.nfs.v3.xdr.dirlistplus3;
import org.dcache.nfs.v3.xdr.SYMLINK3resok;
import org.dcache.nfs.v3.xdr.READDIR3resok;
import org.dcache.nfs.v3.xdr.entry3;
import org.dcache.nfs.v3.xdr.READ3args;
import org.dcache.nfs.v3.xdr.LOOKUP3args;
import org.dcache.nfs.v3.xdr.PATHCONF3res;
import org.dcache.nfs.v3.xdr.LINK3args;
import org.dcache.nfs.v3.xdr.REMOVE3res;
import org.dcache.nfs.v3.xdr.READ3resok;
import org.dcache.nfs.v3.xdr.sattr3;
import org.dcache.nfs.v3.xdr.count3;
import org.dcache.nfs.v3.xdr.MKNOD3args;
import org.dcache.nfs.v3.xdr.READ3res;
import org.dcache.nfs.v3.xdr.READLINK3resok;
import org.dcache.nfs.v3.xdr.cookie3;
import org.dcache.nfs.v3.xdr.LOOKUP3resok;
import org.dcache.nfs.v3.xdr.READDIR3resfail;
import org.dcache.nfs.v3.xdr.RMDIR3res;
import org.dcache.nfs.v3.xdr.RMDIR3resfail;
import org.dcache.nfs.v3.xdr.WRITE3resok;
import org.dcache.nfs.v3.xdr.REMOVE3resfail;
import org.dcache.nfs.v3.xdr.WRITE3res;
import org.dcache.nfs.v3.xdr.wcc_data;
import org.dcache.nfs.v3.xdr.nfs3_prot;
import org.dcache.nfs.v3.xdr.MKDIR3resfail;
import org.dcache.nfs.v3.xdr.RENAME3resok;
import org.dcache.nfs.v3.xdr.dirlist3;
import org.dcache.nfs.v3.xdr.READDIRPLUS3args;
import org.dcache.nfs.v3.xdr.MKDIR3args;
import org.dcache.nfs.v3.xdr.fattr3;
import org.dcache.nfs.v3.xdr.MKNOD3res;
import org.dcache.nfs.v3.xdr.fileid3;
import org.dcache.nfs.v3.xdr.SETATTR3resfail;
import org.dcache.nfs.v3.xdr.uint32;
import org.dcache.nfs.v3.xdr.entryplus3;
import org.dcache.nfs.v3.xdr.pre_op_attr;
import org.dcache.nfs.v3.xdr.SETATTR3args;
import org.dcache.nfs.v3.xdr.SYMLINK3res;
import org.dcache.nfs.v3.xdr.PATHCONF3args;
import org.dcache.nfs.v3.xdr.writeverf3;
import org.dcache.nfs.v3.xdr.RENAME3args;
import org.dcache.nfs.v3.xdr.SYMLINK3args;
import org.dcache.nfs.v3.xdr.READDIRPLUS3resfail;
import org.dcache.nfs.v3.xdr.nfs_fh3;
import org.dcache.nfs.v3.xdr.REMOVE3resok;
import org.dcache.nfs.v3.xdr.READLINK3res;
import org.dcache.nfs.v3.xdr.RENAME3res;
import org.dcache.nfs.v3.xdr.RMDIR3args;
import org.dcache.nfs.v3.xdr.READDIRPLUS3resok;
import org.dcache.nfs.v3.xdr.cookieverf3;
import org.dcache.nfs.v3.xdr.nfs3_protServerStub;
import org.dcache.nfs.v3.xdr.READDIRPLUS3res;
import org.dcache.nfs.v3.xdr.nfstime3;
import org.dcache.nfs.v3.xdr.LINK3resok;
import org.dcache.nfs.v3.xdr.size3;
import org.dcache.nfs.v3.xdr.REMOVE3args;
import org.dcache.nfs.v3.xdr.wcc_attr;
import org.dcache.nfs.v3.xdr.SETATTR3res;
import org.dcache.nfs.v3.xdr.LINK3res;
import org.dcache.nfs.v3.xdr.SETATTR3resok;
import org.dcache.nfs.v3.xdr.READDIR3res;
import org.dcache.nfs.v3.xdr.PATHCONF3resok;
import org.dcache.nfs.v3.xdr.nfspath3;
import org.dcache.nfs.v3.xdr.filename3;
import org.dcache.nfs.v3.xdr.FSINFO3res;
import org.dcache.nfs.v3.xdr.GETATTR3resok;
import org.dcache.nfs.v3.xdr.CREATE3args;
import org.dcache.nfs.v3.xdr.CREATE3resok;
import org.dcache.nfs.v3.xdr.FSSTAT3args;
import org.dcache.nfs.v3.xdr.FSSTAT3resok;
import org.dcache.nfs.v3.xdr.FSINFO3args;
import org.dcache.nfs.v3.xdr.CREATE3res;
import org.dcache.nfs.v3.xdr.GETATTR3args;
import org.dcache.nfs.v3.xdr.ACCESS3resfail;
import org.dcache.nfs.v3.xdr.GETATTR3res;
import org.dcache.nfs.v3.xdr.COMMIT3res;
import org.dcache.nfs.v3.xdr.FSINFO3resok;
import org.dcache.nfs.v3.xdr.ACCESS3resok;
import org.dcache.nfs.v3.xdr.FSSTAT3res;
import org.dcache.nfs.v3.xdr.COMMIT3args;
import org.dcache.nfs.v3.xdr.ACCESS3args;
import org.dcache.nfs.v3.xdr.CREATE3resfail;
import org.dcache.nfs.v3.xdr.FSINFO3resfail;
import org.dcache.nfs.v3.xdr.ACCESS3res;
import org.dcache.nfs.v3.xdr.COMMIT3resok;
import java.io.IOException;
import java.util.Iterator;

import org.dcache.nfs.v3.xdr.COMMIT3resfail;
import org.dcache.nfs.v3.xdr.FSSTAT3resfail;
import org.dcache.nfs.v3.xdr.MKNOD3resfail;
import org.dcache.nfs.v3.xdr.READLINK3resfail;
import org.dcache.nfs.v3.xdr.RENAME3resfail;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.status.*;
import org.dcache.utils.Bytes;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcache.nfs.v3.HimeraNfsUtils.defaultPostOpAttr;
import static org.dcache.nfs.v3.HimeraNfsUtils.defaultWccData;
import static org.dcache.nfs.v3.NameUtils.checkFilename;

import org.dcache.nfs.vfs.FsStat;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.PseudoFs;
import org.dcache.nfs.vfs.DirectoryStream;

import javax.security.auth.Subject;

public class NfsServerV3 extends nfs3_protServerStub {

    // needed to calculate replay size for READDIR3 and READDIRPLUS3
    private static final int ENTRY3_SIZE = 24;
    private static final int ENTRYPLUS3_SIZE = 124;
    private static final int READDIR3RESOK_SIZE = 104;
    private static final int READDIRPLUS3RESOK_SIZE = 104;
    private static final Logger _log = LoggerFactory.getLogger(NfsServerV3.class);

    private final VirtualFileSystem _vfs;
    private final ExportFile _exports;

    private final writeverf3 writeVerifier = generateInstanceWriteVerifier();

    public NfsServerV3(ExportFile exports, VirtualFileSystem fs) throws OncRpcException, IOException {
        _vfs = fs;
        _exports = exports;
    }

    private static writeverf3 generateInstanceWriteVerifier() {
        writeverf3 verf = new writeverf3();
        verf.value = new byte[nfs3_prot.NFS3_WRITEVERFSIZE];
        Bytes.putLong(verf.value, 0, System.currentTimeMillis()); //so long as we dont restart within the same millisecond
        return verf;
    }

    @Override
    public ACCESS3res NFSPROC3_ACCESS_3(RpcCall call$, ACCESS3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        ACCESS3res res = new ACCESS3res();
        _log.debug("NFS Request ACCESS uid: {}", call$.getCredential());

        try {

            res.status = nfsstat.NFS_OK;
            res.resok = new ACCESS3resok();

            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            Inode inode = new Inode(arg1.object.data);
            Stat objStat = fs.getattr(inode);

            HimeraNfsUtils.fill_attributes(objStat, res.resok.obj_attributes.attributes);

            int realAccess = fs.access(inode,  arg1.access.value);

            res.resok.access = new uint32(realAccess);
        } catch (ChimeraNFSException hne) {
            _log.error("ACCESS: {}", hne);
            res.status = hne.getStatus();
            res.resfail = new ACCESS3resfail();
            res.resfail.obj_attributes = defaultPostOpAttr();
        } catch (Exception e) {
            _log.error("ACCESS", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new ACCESS3resfail();
            res.resfail.obj_attributes = defaultPostOpAttr();
        }

        return res;
    }

    @Override
    public COMMIT3res NFSPROC3_COMMIT_3(RpcCall call$, COMMIT3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        COMMIT3res res = new COMMIT3res();

        try {
            Inode inode = new Inode(arg1.file.data);
            long offset = arg1.offset.value.value;
            int count = arg1.count.value.value;

            fs.commit(inode, offset, count);

            res.resok = new COMMIT3resok();

            res.resok.file_wcc = new wcc_data();
            res.resok.file_wcc.after = new post_op_attr();
            res.resok.file_wcc.after.attributes_follow = true;
            res.resok.file_wcc.after.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.file_wcc.after.attributes);
            res.resok.file_wcc.before = new pre_op_attr();
            res.resok.file_wcc.before.attributes_follow = false;
            res.resok.verf = writeVerifier;

        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new COMMIT3resfail();
            res.resfail.file_wcc = defaultWccData();
        } catch (Exception e) {
            _log.error("COMMIT", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new COMMIT3resfail();
            res.resfail.file_wcc = defaultWccData();
        }

        return res;

    }

    @Override
    public CREATE3res NFSPROC3_CREATE_3(RpcCall call$, CREATE3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request CREATE3 uid: {}", call$.getCredential());

        CREATE3res res = new CREATE3res();
        String path = arg1.where.name.value;
        try {

            checkFilename(path);

            Inode parent = new Inode(arg1.where.dir.data);

            sattr3 newAttr = null;
            int mode = arg1.how.mode;

            if ((mode == createmode3.UNCHECKED) || (mode == createmode3.GUARDED)) {
                newAttr = arg1.how.obj_attributes;
            }

            Inode inode = null;
            Stat parentStat = null;
            boolean exists = true;
            long now = System.currentTimeMillis();

            try {
                inode = fs.lookup(parent, path);
            } catch (NoEntException e) {
                exists = false;
            }

            if (exists && (mode != createmode3.UNCHECKED)) {
                throw new ExistException("File alredy exist.");
            }

            parentStat = fs.getattr(parent);

            int fmode = 0644 | Stat.S_IFREG;
            Subject actualSubject = null;
            if (newAttr != null) {
                fmode = newAttr.mode.mode.value.value | Stat.S_IFREG;
                if( newAttr.uid.set_it || newAttr.gid.set_it) {
                    actualSubject = Subjects.of(newAttr.uid.uid.value.value, newAttr.gid.gid.value.value);
                }
            }
            inode = fs.create(parent, Stat.Type.REGULAR, path, actualSubject, fmode);
            Stat inodeStat = fs.getattr(inode);


            res.status = nfsstat.NFS_OK;
            res.resok = new CREATE3resok();
            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(inodeStat, res.resok.obj_attributes.attributes);
            res.resok.obj = new post_op_fh3();
            res.resok.obj.handle_follows = true;
            res.resok.obj.handle = new nfs_fh3();
            res.resok.obj.handle.data = inode.toNfsHandle();

            res.resok.dir_wcc = new wcc_data();
            res.resok.dir_wcc.after = new post_op_attr();
            res.resok.dir_wcc.after.attributes_follow = true;
            res.resok.dir_wcc.after.attributes = new fattr3();

            // correct parent modification time and nlink counter
            parentStat.setNlink(parentStat.getNlink() + 1);
            parentStat.setMTime(now);

            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.after.attributes);

            res.resok.dir_wcc.before = new pre_op_attr();
            res.resok.dir_wcc.before.attributes_follow = false;

        } catch (ChimeraNFSException hne) {

            _log.debug(hne.getMessage());
            res.resfail = new CREATE3resfail();
            res.resfail.dir_wcc = defaultWccData();
            res.status = hne.getStatus();
        } catch (Exception e) {
            _log.error("create", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new CREATE3resfail();
            res.resfail.dir_wcc = defaultWccData();
        }

        return res;
    }

    @Override
    public FSINFO3res NFSPROC3_FSINFO_3(RpcCall call$, FSINFO3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request FSINFO from: {}", call$.getCredential());

        FSINFO3res res = new FSINFO3res();

        try {
            Inode inode = new Inode(arg1.fsroot.data);
            res.status = nfsstat.NFS_OK;
            res.resok = new FSINFO3resok();

            // max size of READ request supported by server
            res.resok.rtmax = new uint32(32768);
            // preferred size of READ request
            res.resok.rtpref = new uint32(32768);
            // suggested multiple for the size of READ request
            res.resok.rtmult = new uint32(8);
            // max size of WRITE request supported by server
            res.resok.wtmax = new uint32(32768);
            // preferred size of WRITE request
            res.resok.wtpref = new uint32(32768);
            // suggested multiple of WRITE request
            res.resok.wtmult = new uint32(8);
            // preferred size of READDIR request
            res.resok.dtpref = new uint32(8192);
            // max size of a file of the file system
            res.resok.maxfilesize = new size3(new uint64(4294967296L));
            // server time granularity -- accurate only to nearest second
            nfstime3 time = new nfstime3();
            time.seconds = new uint32(1);
            time.nseconds = new uint32(0);
            res.resok.time_delta = time;

            // obj_attributes
            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_attributes.attributes);

            res.resok.properties = new uint32(nfs3_prot.FSF3_CANSETTIME |
                    nfs3_prot.FSF3_HOMOGENEOUS |
                    nfs3_prot.FSF3_LINK |
                    nfs3_prot.FSF3_SYMLINK);

        } catch (Exception e) {
            _log.error("FSINFO", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new FSINFO3resfail();
            res.resfail.obj_attributes = defaultPostOpAttr();
        }

        return res;
    }

    @Override
    public FSSTAT3res NFSPROC3_FSSTAT_3(RpcCall call$, FSSTAT3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        FSSTAT3res res = new FSSTAT3res();

        try {

            res.status = nfsstat.NFS_OK;
            res.resok = new FSSTAT3resok();

            FsStat fsStat = fs.getFsStat();
            res.resok.tbytes = new size3(new uint64(fsStat.getTotalSpace()));
            res.resok.fbytes = new size3(new uint64(fsStat.getTotalSpace() - fsStat.getUsedSpace()));
            res.resok.abytes = new size3(new uint64(fsStat.getTotalSpace() - fsStat.getUsedSpace()));

            res.resok.tfiles = new size3(new uint64(fsStat.getTotalFiles()));
            res.resok.ffiles = new size3(new uint64(fsStat.getTotalFiles() - fsStat.getUsedFiles()));
            res.resok.afiles = new size3(new uint64(fsStat.getTotalFiles() - fsStat.getUsedFiles()));

            res.resok.invarsec = new uint32(0);

            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            Inode inode = new Inode(arg1.fsroot.data);

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_attributes.attributes);

        } catch (Exception e) {
            _log.error("FSSTAT", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new FSSTAT3resfail();
            res.resfail.obj_attributes = defaultPostOpAttr();
        }

        return res;

    }

    @Override
    public GETATTR3res NFSPROC3_GETATTR_3(RpcCall call$, GETATTR3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request GETTATTR3 uid: {}", call$.getCredential());

        GETATTR3res res = new GETATTR3res();

        try{
            Inode inode = new Inode(arg1.object.data);
            _log.debug("NFS Request GETATTR for inode: {}", inode.toString());

            res.status = nfsstat.NFS_OK;
            res.resok = new GETATTR3resok();

            res.resok.obj_attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_attributes);

        } catch (ChimeraNFSException e) {
            res.status = e.getStatus();
        } catch (Exception e) {
            _log.error("GETATTR", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
        }

        return res;
    }

    @Override
    public LINK3res NFSPROC3_LINK_3(RpcCall call$, LINK3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request LINK3 uid: {}", call$.getCredential());

        LINK3res res = new LINK3res();
        try {

            Inode parent = new Inode(arg1.link.dir.data);
            String name = arg1.link.name.value;
            checkFilename(name);

            Inode hlink = new Inode(arg1.file.data);

            Stat parentStat = fs.getattr(parent);
            fs.link(parent, hlink, name, call$.getCredential().getSubject());

            res.resok = new LINK3resok();
            res.resok.file_attributes = new post_op_attr();
            res.resok.file_attributes.attributes_follow = true;
            res.resok.file_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(hlink), res.resok.file_attributes.attributes);

            res.resok.linkdir_wcc = new wcc_data();
            res.resok.linkdir_wcc.after = new post_op_attr();
            res.resok.linkdir_wcc.after.attributes_follow = true;
            res.resok.linkdir_wcc.after.attributes = new fattr3();

            // fake answer
            parentStat.setNlink(parentStat.getNlink() + 1);
            parentStat.setMTime(System.currentTimeMillis());
            HimeraNfsUtils.fill_attributes(parentStat, res.resok.linkdir_wcc.after.attributes);

            res.resok.linkdir_wcc.before = new pre_op_attr();
            res.resok.linkdir_wcc.before.attributes_follow = false;

            res.status = nfsstat.NFS_OK;

        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new LINK3resfail();
            res.resfail.file_attributes = defaultPostOpAttr();
            res.resfail.linkdir_wcc = defaultWccData();
        } catch (Exception e) {
            _log.error("LINK", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail.file_attributes = defaultPostOpAttr();
            res.resfail.linkdir_wcc = defaultWccData();
        }

        return res;
    }

    @Override
    public LOOKUP3res NFSPROC3_LOOKUP_3(RpcCall call$, LOOKUP3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        LOOKUP3res res = new LOOKUP3res();

        try {
            Inode parent = new Inode(arg1.what.dir.data);
            String name = arg1.what.name.value;

            checkFilename(name);

            Inode inode = fs.lookup(parent, name);

            res.status = nfsstat.NFS_OK;
            res.resok = new LOOKUP3resok();

            nfs_fh3 fh3 = new nfs_fh3();
            fh3.data = inode.toNfsHandle();
            res.resok.object = fh3;

            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_attributes.attributes);

            res.resok.dir_attributes = new post_op_attr();
            res.resok.dir_attributes.attributes_follow = true;
            res.resok.dir_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(parent), res.resok.dir_attributes.attributes);

        } catch (ChimeraNFSException hne) {
            _log.debug("lookup {}", hne.toString());
            res.status = hne.getStatus();
            res.resfail = new LOOKUP3resfail();
            res.resfail.dir_attributes = defaultPostOpAttr();
        } catch (Exception e) {
            _log.error("LOOKUP", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new LOOKUP3resfail();
            res.resfail.dir_attributes = defaultPostOpAttr();
        }

        return res;
    }

    @Override
    public MKDIR3res NFSPROC3_MKDIR_3(RpcCall call$, MKDIR3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request MKDIR3 uid: {}", call$.getCredential());

        MKDIR3res res = new MKDIR3res();
        try {
            Inode parent = new Inode(arg1.where.dir.data);

            String name = arg1.where.name.value;
            checkFilename(name);

            sattr3 attr = arg1.attributes;

            Stat parentStat = fs.getattr(parent);

            int mode = 0777;
            Subject actualSubject = null;
            if (attr != null) {
                mode = attr.mode.mode.value.value | Stat.S_IFDIR;
                if( attr.uid.set_it || attr.gid.set_it) {
                    actualSubject = Subjects.of(attr.uid.uid.value.value, attr.gid.gid.value.value);
                }
            }

            Inode inode = fs.mkdir(parent, name, actualSubject, mode);

            res.resok = new MKDIR3resok();
            res.resok.obj = new post_op_fh3();
            res.resok.obj.handle_follows = true;
            res.resok.obj.handle = new nfs_fh3();
            res.resok.obj.handle.data = inode.toNfsHandle();

            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_attributes.attributes);

            res.resok.dir_wcc = new wcc_data();
            res.resok.dir_wcc.after = new post_op_attr();
            res.resok.dir_wcc.after.attributes_follow = true;
            res.resok.dir_wcc.after.attributes = new fattr3();

            // fake answer
            parentStat.setNlink(parentStat.getNlink() + 1);
            parentStat.setMTime(System.currentTimeMillis());
            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.after.attributes);

            res.resok.dir_wcc.before = new pre_op_attr();
            res.resok.dir_wcc.before.attributes_follow = false;

            res.status = nfsstat.NFS_OK;

        } catch (ChimeraNFSException hne) {
            res.resfail = new MKDIR3resfail();
            res.resfail.dir_wcc = defaultWccData();
            res.status = hne.getStatus();
        } catch (Exception e) {
            _log.error("MKDIR", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new MKDIR3resfail();
            res.resfail.dir_wcc = defaultWccData();
        }

        return res;
    }

    @Override
    public MKNOD3res NFSPROC3_MKNOD_3(RpcCall call$, MKNOD3args arg1) {

        MKNOD3res res = new MKNOD3res();
        res.status = nfsstat.NFSERR_NOTSUPP;
        res.resfail = new MKNOD3resfail();
        res.resfail.dir_wcc = defaultWccData();
        return res;

    }

    @Override
    public void NFSPROC3_NULL_3(RpcCall call$) {
    }

    @Override
    public PATHCONF3res NFSPROC3_PATHCONF_3(RpcCall call$, PATHCONF3args arg1) {

        PATHCONF3res res = new PATHCONF3res();

        res.resok = new PATHCONF3resok();
        res.resok.case_insensitive = false;
        res.resok.case_preserving = true;
        res.resok.chown_restricted = false;
        res.resok.no_trunc = true;
        res.resok.linkmax = new uint32(512);
        res.resok.name_max = new uint32(256);

        res.resok.obj_attributes = new post_op_attr();
        res.resok.obj_attributes.attributes_follow = false;

        res.status = nfsstat.NFS_OK;

        return res;

    }

    /*
     * to simulate snapshot-like list following trick is used:
     *
     *   1. for each new readdir(plus) ( cookie == 0 ) generate new cookie verifier
     *   2. list result stored in timed Map, where verifier used as a key
     *
     */
    @Override
    public READDIRPLUS3res NFSPROC3_READDIRPLUS_3(RpcCall call$, READDIRPLUS3args arg1) {

        final VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request READDIRPLUS3 uid: {}", call$.getCredential());

        READDIRPLUS3res res = new READDIRPLUS3res();

        try {

            final Inode dir = new Inode(arg1.dir.data);

            Stat dirStat = fs.getattr(dir);

            if (dirStat.type() != Stat.Type.DIRECTORY) {
                throw new NotDirException("Path is not a directory.");
            }

            long startValue = arg1.cookie.value.value;
            DirectoryStream directoryStream;
            cookieverf3 cookieverf = arg1.cookieverf;

            directoryStream = fs.list(dir, cookieverf.value, startValue);
            Iterator<DirectoryEntry> dirList = directoryStream.iterator();

            res.status = nfsstat.NFS_OK;
            res.resok = new READDIRPLUS3resok();
            res.resok.reply = new dirlistplus3();
            res.resok.dir_attributes = new post_op_attr();
            res.resok.dir_attributes.attributes_follow = true;
            res.resok.dir_attributes.attributes = new fattr3();
            res.resok.cookieverf = new cookieverf3(directoryStream.getVerifier());

            HimeraNfsUtils.fill_attributes(dirStat, res.resok.dir_attributes.attributes);


            int currcount = READDIRPLUS3RESOK_SIZE;
            int dircount = 0;
            int fcount = 0;
            entryplus3 lastEntry = null;

            while (dirList.hasNext()) {

                fcount++;
                DirectoryEntry le = dirList.next();
                String name = le.getName();
                Inode ef = le.getInode();

                entryplus3 currentEntry = new entryplus3();
                currentEntry.fileid = new fileid3(new uint64(le.getStat().getFileId()));
                currentEntry.name = new filename3(name);
                currentEntry.cookie = new cookie3(new uint64(le.getCookie()));
                currentEntry.name_handle = new post_op_fh3();
                currentEntry.name_handle.handle_follows = true;
                currentEntry.name_handle.handle = new nfs_fh3();
                currentEntry.name_handle.handle.data = ef.toNfsHandle();
                currentEntry.name_attributes = new post_op_attr();
                currentEntry.name_attributes.attributes_follow = true;
                currentEntry.name_attributes.attributes = new fattr3();
                HimeraNfsUtils.fill_attributes(le.getStat(), currentEntry.name_attributes.attributes);

                // check if writing this entry exceeds the count limit
                int newSize = ENTRYPLUS3_SIZE + name.length() + currentEntry.name_handle.handle.data.length;
                int newDirSize = name.length();
                if ((currcount + newSize > arg1.maxcount.value.value) || (dircount + newDirSize > arg1.dircount.value.value)) {
                    if (lastEntry == null) {
                        //corner case - means we didnt have enough space to
                        //write even a single entry.
                        throw new TooSmallException("can't send even a single entry");
                    }
                    break;
                }

                dircount += newDirSize;
                currcount += newSize;

                if (lastEntry == null) {
                    res.resok.reply.entries = currentEntry;
                } else {
                    lastEntry.nextentry = currentEntry;
                }
                lastEntry = currentEntry;
            }

            res.resok.reply.eof = !dirList.hasNext();
            _log.debug("Sending {} entries ( {} bytes from {}, dircount = {} from {} ) cookie = {}",
                    fcount, currcount,
                    arg1.maxcount.value.value, dircount,
                    arg1.dircount.value.value,
                    startValue
            );

        } catch (ChimeraNFSException hne) {
            _log.debug("READDIRPLUS3 status: {}", hne.toString());
            res.resfail = new READDIRPLUS3resfail();
            res.resfail.dir_attributes = defaultPostOpAttr();
            res.status = hne.getStatus();
        } catch (Exception e) {
            _log.error("READDIRPLUS3", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new READDIRPLUS3resfail();
            res.resfail.dir_attributes = defaultPostOpAttr();
        }

        return res;
    }

    @Override
    public READDIR3res NFSPROC3_READDIR_3(RpcCall call$, READDIR3args arg1) {

        final VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request READDIR3 uid: {}", call$.getCredential());

        READDIR3res res = new READDIR3res();

        try {

            final Inode dir = new Inode(arg1.dir.data);

            Stat dirStat = fs.getattr(dir);

            if (dirStat.type() != Stat.Type.DIRECTORY) {
                throw new NotDirException("Path is not a directory.");
            }

            long startValue = arg1.cookie.value.value;
            DirectoryStream directoryStream;
            cookieverf3 cookieverf = arg1.cookieverf;

            directoryStream = fs.list(dir, cookieverf.value, startValue);
            Iterator<DirectoryEntry> dirList = directoryStream.iterator();

            res.status = nfsstat.NFS_OK;
            res.resok = new READDIR3resok();
            res.resok.reply = new dirlist3();
            res.resok.dir_attributes = new post_op_attr();
            res.resok.dir_attributes.attributes_follow = true;
            res.resok.dir_attributes.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(dirStat, res.resok.dir_attributes.attributes);

            res.resok.cookieverf = new cookieverf3(directoryStream.getVerifier());

            int currcount = READDIR3RESOK_SIZE;
            int fcount = 0;
            entry3 lastEntry = null;

            while (dirList.hasNext()) {

                fcount++;
                DirectoryEntry le = dirList.next();
                String name = le.getName();

                entry3 currentEntry = new entry3();
                currentEntry.fileid = new fileid3(new uint64(le.getStat().getFileId()));
                currentEntry.name = new filename3(name);
                currentEntry.cookie = new cookie3(new uint64(le.getCookie()));

                // check if writing this entry exceeds the count limit
                int newSize = ENTRY3_SIZE + name.length();
                if (currcount + newSize > arg1.count.value.value) {
                    if (lastEntry == null) {
                        //corner case - means we didnt have enough space to
                        //write even a single entry.
                        throw new TooSmallException("can't send even a single entry");
                    }
                    break;
                }
                currcount += newSize;

                if (lastEntry == null) {
                    res.resok.reply.entries = currentEntry;
                } else {
                    lastEntry.nextentry = currentEntry;
                }
                lastEntry = currentEntry;
            }

            res.resok.reply.eof = !dirList.hasNext();
            _log.debug("Sending {} entries ( {} bytes from {}) cookie = {}",
                    fcount, currcount,
                    arg1.count.value.value,
                    startValue
            );

        } catch (ChimeraNFSException hne) {
            _log.error("READDIR: {}", hne.toString());
            res.resfail = new READDIR3resfail();
            res.resfail.dir_attributes = defaultPostOpAttr();
            res.status = hne.getStatus();
        } catch (Exception e) {
            _log.error("READDIR", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new READDIR3resfail();
            res.resfail.dir_attributes = defaultPostOpAttr();
        }

        return res;
    }

    @Override
    public READLINK3res NFSPROC3_READLINK_3(RpcCall call$, READLINK3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        READLINK3res res = new READLINK3res();


        try {
            Inode inode = new Inode(arg1.symlink.data);

            res.resok = new READLINK3resok();
            res.resok.data = new nfspath3(fs.readlink(inode));
            res.resok.symlink_attributes = new post_op_attr();

            res.resok.symlink_attributes.attributes_follow = true;
            res.resok.symlink_attributes.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.symlink_attributes.attributes);

            res.status = nfsstat.NFS_OK;

        } catch (ChimeraNFSException e) {
            _log.error("READLINK", e);
            res.status = e.getStatus();
            res.resfail = new READLINK3resfail();
            res.resfail.symlink_attributes = defaultPostOpAttr();
        } catch (Exception e) {
            _log.error("READLINK", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new READLINK3resfail();
            res.resfail.symlink_attributes = defaultPostOpAttr();
        }

        return res;

    }

    @Override
    public READ3res NFSPROC3_READ_3(RpcCall call$, READ3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        READ3res res = new READ3res();

        try {
            Inode inode = new Inode(arg1.file.data);
            long offset = arg1.offset.value.value;
            int count = arg1.count.value.value;

            Stat inodeStat = fs.getattr(inode);

            res.resok = new READ3resok();
            res.resok.data = new byte[count];

            res.resok.count = new count3();
            res.resok.count.value = new uint32();

            byte[] b = new byte[count];
            res.resok.count.value.value = fs.read(inode, b, offset, count);
            if (res.resok.count.value.value < 0) {
                throw new NfsIoException("IO not allowed");
            }
            if (res.resok.count.value.value == count) {
                res.resok.data = b;
            } else {
                res.resok.data = new byte[res.resok.count.value.value];
                System.arraycopy(b, 0, res.resok.data, 0, res.resok.count.value.value);
            }

            if (res.resok.count.value.value + offset == inodeStat.getSize()) {
                res.resok.eof = true;
            }

            res.resok.file_attributes = new post_op_attr();
            res.resok.file_attributes.attributes_follow = true;
            res.resok.file_attributes.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(inodeStat, res.resok.file_attributes.attributes);
        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new READ3resfail();
            res.resfail.file_attributes = defaultPostOpAttr();
        } catch (Exception e) {
            _log.error("READ", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new READ3resfail();
            res.resfail.file_attributes = defaultPostOpAttr();
        }

        return res;

    }

    @Override
    public REMOVE3res NFSPROC3_REMOVE_3(RpcCall call$, REMOVE3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request REMOVE3 uid: {}", call$.getCredential());

        REMOVE3res res = new REMOVE3res();

        try {
            Inode parent = new Inode(arg1.object.dir.data);

            String name = arg1.object.name.value;
            checkFilename(name);

            Stat parentStat = fs.getattr(parent);

            fs.remove(parent, name);

            res.resok = new REMOVE3resok();
            res.status = nfsstat.NFS_OK;

            res.resok.dir_wcc = new wcc_data();

            res.resok.dir_wcc.before = new pre_op_attr();
            res.resok.dir_wcc.before.attributes_follow = true;
            res.resok.dir_wcc.before.attributes = new wcc_attr();
            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.before.attributes);


            // correct parent modification time and nlink counter
            parentStat.setMTime(System.currentTimeMillis());
            parentStat.setNlink(parentStat.getNlink() - 1);

            res.resok.dir_wcc.after = new post_op_attr();
            res.resok.dir_wcc.after.attributes_follow = true;
            res.resok.dir_wcc.after.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.after.attributes);


        } catch (ChimeraNFSException hne) {
            res.resfail = new REMOVE3resfail();
            res.resfail.dir_wcc = defaultWccData();
            res.status = hne.getStatus();
        } catch (Exception e) {
            _log.error("REMOVE", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new REMOVE3resfail();
            res.resfail.dir_wcc = defaultWccData();
        }

        return res;

    }

    @Override
    public RENAME3res NFSPROC3_RENAME_3(RpcCall call$, RENAME3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request RENAME3 uid: {}", call$.getCredential());

        RENAME3res res = new RENAME3res();

        try {
            Inode from = new Inode(arg1.from.dir.data);
            String file1 = arg1.from.name.value;
            checkFilename(file1);

            Inode to = new Inode(arg1.to.dir.data);
            String file2 = arg1.to.name.value;
            checkFilename(file2);

            fs.move(from, file1, to, file2);

            res.resok = new RENAME3resok();

            res.resok.fromdir_wcc = new wcc_data();
            res.resok.fromdir_wcc.after = new post_op_attr();
            res.resok.fromdir_wcc.after.attributes_follow = true;
            res.resok.fromdir_wcc.after.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(fs.getattr(from), res.resok.fromdir_wcc.after.attributes);

            res.resok.fromdir_wcc.before = new pre_op_attr();
            res.resok.fromdir_wcc.before.attributes_follow = false;

            res.resok.todir_wcc = new wcc_data();
            res.resok.todir_wcc.after = new post_op_attr();
            res.resok.todir_wcc.after.attributes_follow = true;
            res.resok.todir_wcc.after.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(fs.getattr(to), res.resok.todir_wcc.after.attributes);

            res.resok.todir_wcc.before = new pre_op_attr();
            res.resok.todir_wcc.before.attributes_follow = false;

            res.status = nfsstat.NFS_OK;
        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new RENAME3resfail();
            res.resfail.fromdir_wcc = defaultWccData();
            res.resfail.todir_wcc = defaultWccData();
        } catch (Exception e) {
            _log.error("RENAME", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new RENAME3resfail();
            res.resfail.fromdir_wcc = defaultWccData();
            res.resfail.todir_wcc = defaultWccData();
        }

        return res;

    }

    @Override
    public RMDIR3res NFSPROC3_RMDIR_3(RpcCall call$, RMDIR3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request RMDIR3 uid: {}", call$.getCredential());

        RMDIR3res res = new RMDIR3res();

        try {
            Inode parent = new Inode(arg1.object.dir.data);
            String file = arg1.object.name.value;
            checkFilename(file);

            Inode inode = fs.lookup(parent, file);
            Stat parentStat = fs.getattr(parent);

            fs.remove(parent, file);

            res.resok = new RMDIR3resok();
            res.status = nfsstat.NFS_OK;

            res.resok.dir_wcc = new wcc_data();
            res.resok.dir_wcc.after = new post_op_attr();

            res.resok.dir_wcc.before = new pre_op_attr();
            res.resok.dir_wcc.before.attributes_follow = true;
            res.resok.dir_wcc.before.attributes = new wcc_attr();
            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.before.attributes);

            res.resok.dir_wcc.after.attributes_follow = true;
            res.resok.dir_wcc.after.attributes = new fattr3();

            parentStat.setMTime(System.currentTimeMillis());
            parentStat.setNlink(parentStat.getNlink() - 1);

            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.after.attributes);

        } catch (ChimeraNFSException hne) {
            res.resfail = new RMDIR3resfail();
            res.resfail.dir_wcc = defaultWccData();
            res.status = hne.getStatus();
        } catch (Exception e) {
            _log.error("RMDIR", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new RMDIR3resfail();
            res.resfail.dir_wcc = defaultWccData();
        }

        return res;
    }

    @Override
    public SETATTR3res NFSPROC3_SETATTR_3(RpcCall call$, SETATTR3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request SETATTR3 uid: {}", call$.getCredential());

        SETATTR3res res = new SETATTR3res();

        try {
            Inode inode = new Inode(arg1.object.data);
            sattr3 newAttr = arg1.new_attributes;

            HimeraNfsUtils.set_sattr(inode, fs, newAttr);
            res.resok = new SETATTR3resok();
            res.resok.obj_wcc = new wcc_data();
            res.resok.obj_wcc.after = new post_op_attr();
            res.resok.obj_wcc.after.attributes_follow = true;
            res.resok.obj_wcc.after.attributes = new fattr3();
            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_wcc.after.attributes);

            res.resok.obj_wcc.before = new pre_op_attr();
            res.resok.obj_wcc.before.attributes_follow = false;

            res.status = nfsstat.NFS_OK;
        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new SETATTR3resfail();
            res.resfail.obj_wcc = defaultWccData();
        } catch (Exception e) {
            _log.error("SETATTR", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new SETATTR3resfail();
            res.resfail.obj_wcc = defaultWccData();
        }

        return res;

    }

    @Override
    public SYMLINK3res NFSPROC3_SYMLINK_3(RpcCall call$, SYMLINK3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        _log.debug("NFS Request SYMLINK3 uid: {}", call$.getCredential());

        SYMLINK3res res = new SYMLINK3res();

        try {

            Inode parent = new Inode(arg1.where.dir.data);
            String file = arg1.where.name.value;
            checkFilename(file);

            String link = arg1.symlink.symlink_data.value;
            sattr3 linkAttr = arg1.symlink.symlink_attributes;

            Stat parentStat = fs.getattr(parent);
            Inode inode = fs.symlink(parent, file, link, call$.getCredential().getSubject(), 777);

            HimeraNfsUtils.set_sattr(inode, fs, linkAttr);

            res.resok = new SYMLINK3resok();

            res.resok.obj_attributes = new post_op_attr();
            res.resok.obj_attributes.attributes_follow = true;
            res.resok.obj_attributes.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.obj_attributes.attributes);
            res.resok.obj = new post_op_fh3();
            res.resok.obj.handle_follows = true;
            res.resok.obj.handle = new nfs_fh3();
            res.resok.obj.handle.data = inode.toNfsHandle();

            res.resok.dir_wcc = new wcc_data();
            res.resok.dir_wcc.after = new post_op_attr();
            res.resok.dir_wcc.after.attributes_follow = true;
            res.resok.dir_wcc.after.attributes = new fattr3();

            // fake answer
            parentStat.setNlink(parentStat.getNlink() + 1);
            parentStat.setMTime(System.currentTimeMillis());

            HimeraNfsUtils.fill_attributes(parentStat, res.resok.dir_wcc.after.attributes);

            res.resok.dir_wcc.before = new pre_op_attr();
            res.resok.dir_wcc.before.attributes_follow = false;

            res.status = nfsstat.NFS_OK;

        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new SYMLINK3resfail();
            res.resfail.dir_wcc = defaultWccData();
        } catch (Exception e) {
            _log.error("SYMLINK", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new SYMLINK3resfail();
            res.resfail.dir_wcc = defaultWccData();
        }

        return res;

    }

    @Override
    public WRITE3res NFSPROC3_WRITE_3(RpcCall call$, WRITE3args arg1) {

        VirtualFileSystem fs = new PseudoFs(_vfs, call$, _exports);
        WRITE3res res = new WRITE3res();

        try {
            Inode inode = new Inode(arg1.file.data);
            long offset = arg1.offset.value.value;
            int count = arg1.count.value.value;

            res.resok = new WRITE3resok();
            res.status = nfsstat.NFS_OK;

            VirtualFileSystem.StabilityLevel requiredStabilityLevel = VirtualFileSystem.StabilityLevel.fromStableHow(arg1.stable);
            VirtualFileSystem.WriteResult ret = fs.write(inode, arg1.data, offset, count, requiredStabilityLevel);
            if (ret.getBytesWritten() < 0) {
                throw new NfsIoException("IO not allowed");
            }

            res.resok.count = new count3(new uint32(ret.getBytesWritten()));
            res.resok.file_wcc = new wcc_data();
            res.resok.file_wcc.after = new post_op_attr();
            res.resok.file_wcc.after.attributes_follow = true;
            res.resok.file_wcc.after.attributes = new fattr3();

            HimeraNfsUtils.fill_attributes(fs.getattr(inode), res.resok.file_wcc.after.attributes);
            res.resok.file_wcc.before = new pre_op_attr();
            res.resok.file_wcc.before.attributes_follow = false;
            res.resok.committed = ret.getStabilityLevel().toStableHow();
            res.resok.verf = writeVerifier;
        } catch (ChimeraNFSException hne) {
            res.status = hne.getStatus();
            res.resfail = new WRITE3resfail();
            res.resfail.file_wcc = defaultWccData();
        } catch (Exception e) {
            _log.error("WRITE", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
            res.resfail = new WRITE3resfail();
            res.resfail.file_wcc = defaultWccData();
        }

        return res;

    }
}
