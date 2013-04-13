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
package org.dcache.nfs.v4;

import com.google.common.base.Optional;
import java.io.IOException;
import org.dcache.nfs.v4.xdr.fattr4_numlinks;
import org.dcache.nfs.v4.xdr.fattr4_hidden;
import org.dcache.nfs.v4.xdr.fattr4_system;
import org.dcache.nfs.v4.xdr.fattr4_aclsupport;
import org.dcache.nfs.v4.xdr.nfs_ftype4;
import org.dcache.nfs.v4.xdr.attrlist4;
import org.dcache.nfs.v4.xdr.fattr4_case_insensitive;
import org.dcache.nfs.v4.xdr.nfs_lease4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.fattr4_rawdev;
import org.dcache.nfs.v4.xdr.fattr4_maxname;
import org.dcache.nfs.v4.xdr.fattr4_owner;
import org.dcache.nfs.v4.xdr.fattr4_space_used;
import org.dcache.nfs.v4.xdr.fattr4_maxlink;
import org.dcache.nfs.v4.xdr.fattr4_unique_handles;
import org.dcache.nfs.v4.xdr.fattr4_lease_time;
import org.dcache.nfs.v4.xdr.uint64_t;
import org.dcache.nfs.v4.xdr.fattr4_fh_expire_type;
import org.dcache.nfs.v4.xdr.int64_t;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.v4.xdr.fattr4_named_attr;
import org.dcache.nfs.v4.xdr.specdata4;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.fattr4_homogeneous;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.fattr4_maxread;
import org.dcache.nfs.v4.xdr.fattr4_fs_layout_types;
import org.dcache.nfs.v4.xdr.fattr4_maxwrite;
import org.dcache.nfs.v4.xdr.fattr4_time_create;
import org.dcache.nfs.v4.xdr.fattr4_files_avail;
import org.dcache.nfs.v4.xdr.fattr4_mounted_on_fileid;
import org.dcache.nfs.v4.xdr.fattr4_space_total;
import org.dcache.nfs.v4.xdr.fattr4_fileid;
import org.dcache.nfs.v4.xdr.fattr4_change;
import org.dcache.nfs.v4.xdr.fattr4_symlink_support;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.fattr4_case_preserving;
import org.dcache.nfs.v4.xdr.changeid4;
import org.dcache.nfs.v4.xdr.fattr4_size;
import org.dcache.nfs.v4.xdr.fattr4_files_total;
import org.dcache.nfs.v4.xdr.fattr4_filehandle;
import org.dcache.nfs.v4.xdr.fattr4;
import org.dcache.nfs.v4.xdr.nfstime4;
import org.dcache.nfs.v4.xdr.mode4;
import org.dcache.nfs.v4.xdr.fattr4_link_support;
import org.dcache.nfs.v4.xdr.fattr4_time_modify;
import org.dcache.nfs.v4.xdr.fattr4_no_trunc;
import org.dcache.nfs.v4.xdr.fattr4_rdattr_error;
import org.dcache.nfs.v4.xdr.fattr4_files_free;
import org.dcache.nfs.v4.xdr.fattr4_time_metadata;
import org.dcache.nfs.v4.xdr.fattr4_mode;
import org.dcache.nfs.v4.xdr.fattr4_maxfilesize;
import org.dcache.nfs.v4.xdr.fattr4_acl;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.fattr4_fsid;
import org.dcache.nfs.v4.xdr.fattr4_time_access;
import org.dcache.nfs.v4.xdr.fattr4_supported_attrs;
import org.dcache.nfs.v4.xdr.utf8str_mixed;
import org.dcache.nfs.v4.xdr.fattr4_space_free;
import org.dcache.nfs.v4.xdr.fattr4_cansettime;
import org.dcache.nfs.v4.xdr.fattr4_type;
import org.dcache.nfs.v4.xdr.fsid4;
import org.dcache.nfs.v4.xdr.GETATTR4resok;
import org.dcache.nfs.v4.xdr.GETATTR4res;
import org.dcache.nfs.ChimeraNFSException;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrBuffer;
import org.dcache.chimera.UnixPermission;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.FsStat;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.nfs.vfs.Stat;
import org.dcache.xdr.OncRpcException;
import org.glassfish.grizzly.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationGETATTR extends AbstractNFSv4Operation {

        private static final Logger _log = LoggerFactory.getLogger(OperationGETATTR.class);

	public OperationGETATTR(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_GETATTR);
	}

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException, OncRpcException {

        final GETATTR4res res = result.opgetattr;

        res.resok4 = new GETATTR4resok();
        res.resok4.obj_attributes = getAttributes(_args.opgetattr.attr_request,
                context.getFs(),
                context.currentInode(), context);

        res.status = nfsstat.NFS_OK;

    }

    static fattr4 getAttributes(bitmap4 bitmap, VirtualFileSystem fs, Inode inode, Stat stat, CompoundContext context)
            throws IOException, OncRpcException {

        int[] mask = new int[bitmap.value.length];
        for( int i = 0; i < mask.length; i++) {
            mask[i] = bitmap.value[i].value;
            _log.debug("getAttributes[{}]: {}", i, Integer.toBinaryString(mask[i]) );
        }

        int[] retMask = new int[mask.length];

        XdrBuffer xdr = new XdrBuffer(1024);
        xdr.beginEncoding();

        if( mask.length != 0 ) {
            int maxAttr = 32*mask.length;
            for( int i = 0; i < maxAttr; i++) {

                int newmask = (mask[i/32] >> (i-(32*(i/32))) );
                if( (newmask & 1) > 0 ) {
                        Optional<XdrAble> optionalAttr = (Optional<XdrAble>)fattr2xdr(i, fs, inode, stat, context);
                        if( optionalAttr.isPresent()) {
                            XdrAble attr = optionalAttr.get();
                            _log.debug("   getAttributes : {} ({}) OK.", i, attrMask2String(i) );
                            attr.xdrEncode(xdr);
                            int attrmask = 1 << (i-(32*(i/32)));
                            retMask[i/32] |= attrmask;
                        }else{
                            _log.debug("   getAttributes : {} ({}) NOT SUPPORTED.", i, attrMask2String(i) );
                        }
                }

            }

        }
        xdr.endEncoding();
        Buffer body = xdr.asBuffer();
        byte[] retBytes = new byte[body.remaining()] ;
        body.get(retBytes);

        fattr4 attributes = new fattr4();
        attributes.attrmask = new bitmap4();
        attributes.attrmask.value = new uint32_t[retMask.length];
        for( int i = 0; i < retMask.length; i++) {
            attributes.attrmask.value[i] = new uint32_t(retMask[i]);
            _log.debug("getAttributes[{}] reply : {}", i, Integer.toBinaryString(retMask[i]) );

        }
        attributes.attr_vals = new attrlist4(retBytes);

        return attributes;

    }

    static fattr4  getAttributes(bitmap4 bitmap, VirtualFileSystem fs, Inode inode, CompoundContext context)
            throws IOException, OncRpcException {
        return getAttributes(bitmap, fs, inode, context.getFs().getattr(inode), context);
    }

    private static FsStat getFsStat(FsStat fsStat, VirtualFileSystem fs) throws IOException {
        if (fsStat != null) {
            return fsStat;
        }
        return fs.getFsStat();
    }

    /**
     * get inodes requested attribute and converted into RPC xdr format
     * operates with READ and R/W attributes
     *
     * @param fattr
     * @param inode
     * @return XdrAble of object attribute,
     * Corresponding to fattr
     * @throws Exception
     */

    // read/read-write
    private static Optional<? extends XdrAble> fattr2xdr(int fattr, VirtualFileSystem fs, Inode inode, Stat stat, CompoundContext context) throws IOException {

        FsStat fsStat = null;

        switch (fattr) {

            case nfs4_prot.FATTR4_SUPPORTED_ATTRS:
                bitmap4 bitmap = new bitmap4();
                bitmap.value = new uint32_t[2];
                bitmap.value[0] = new uint32_t(NFSv4FileAttributes.NFS4_SUPPORTED_ATTRS_MASK0);
                bitmap.value[1] = new uint32_t(NFSv4FileAttributes.NFS4_SUPPORTED_ATTRS_MASK1);
                return Optional.of(new fattr4_supported_attrs(bitmap));
            case nfs4_prot.FATTR4_TYPE:
                fattr4_type type = new fattr4_type(unixType2NFS(stat.getMode()));
                return Optional.of(type);
            case nfs4_prot.FATTR4_FH_EXPIRE_TYPE:
                uint32_t fh_type = new uint32_t(nfs4_prot.FH4_PERSISTENT);
                fattr4_fh_expire_type fh_expire_type = new fattr4_fh_expire_type(fh_type);
                return Optional.of(fh_expire_type);
            case nfs4_prot.FATTR4_CHANGE:
                changeid4 cid = new changeid4(new uint64_t(stat.getCTime()));
                fattr4_change change = new fattr4_change(cid);
                return Optional.of(change);
            case nfs4_prot.FATTR4_SIZE:
                fattr4_size size = new fattr4_size(new uint64_t(stat.getSize()));
                return Optional.of(size);
            case nfs4_prot.FATTR4_LINK_SUPPORT:
                fattr4_link_support link_support = new fattr4_link_support(true);
                return Optional.of(link_support);
            case nfs4_prot.FATTR4_SYMLINK_SUPPORT:
                fattr4_symlink_support symlink_support = new fattr4_symlink_support(true);
                return Optional.of(symlink_support);
            case nfs4_prot.FATTR4_NAMED_ATTR:
                fattr4_named_attr named_attr = new fattr4_named_attr(false);
                return Optional.of(named_attr);
            case nfs4_prot.FATTR4_FSID:
                fsid4 fsid = new fsid4();
                fsid.major = new uint64_t(17);
                fsid.minor = new uint64_t(17);
                return Optional.of(new fattr4_fsid(fsid));
            case nfs4_prot.FATTR4_UNIQUE_HANDLES:
                return Optional.of(new fattr4_unique_handles(true));
            case nfs4_prot.FATTR4_LEASE_TIME:
                return Optional.of(new fattr4_lease_time(new nfs_lease4(new uint32_t(NFSv4Defaults.NFS4_LEASE_TIME))));
            case nfs4_prot.FATTR4_RDATTR_ERROR:
                //enum is an integer
                return Optional.of(new fattr4_rdattr_error(0));
            case nfs4_prot.FATTR4_FILEHANDLE:
                nfs_fh4 fh = new nfs_fh4();
                fh.value = inode.toNfsHandle();
                return Optional.of(new fattr4_filehandle(fh));
            case nfs4_prot.FATTR4_ACL:
                nfsace4[] aces = context.getFs().getAcl(inode);
                return Optional.of(new fattr4_acl(aces));
            case nfs4_prot.FATTR4_ACLSUPPORT:
                fattr4_aclsupport aclSupport = new fattr4_aclsupport();
                aclSupport.value = new uint32_t();
                aclSupport.value.value = nfs4_prot.ACL4_SUPPORT_ALLOW_ACL
                        | nfs4_prot.ACL4_SUPPORT_DENY_ACL;
                return Optional.of(aclSupport);
            case nfs4_prot.FATTR4_ARCHIVE:
                return Optional.absent();
            case nfs4_prot.FATTR4_CANSETTIME:
                return Optional.of(new fattr4_cansettime(true));
            case nfs4_prot.FATTR4_CASE_INSENSITIVE:
                return Optional.of(new fattr4_case_insensitive(true));
            case nfs4_prot.FATTR4_CASE_PRESERVING:
                return Optional.of(new fattr4_case_preserving(true));
            case nfs4_prot.FATTR4_CHOWN_RESTRICTED:
                return Optional.absent();
            case nfs4_prot.FATTR4_FILEID:
                return Optional.of(new fattr4_fileid(new uint64_t(stat.getFileId())));
            case nfs4_prot.FATTR4_FILES_AVAIL:
                fsStat = getFsStat(fsStat, fs);
                fattr4_files_avail files_avail = new fattr4_files_avail(new uint64_t(fsStat.getTotalFiles() - fsStat.getUsedFiles()));
                return Optional.of(files_avail);
            case nfs4_prot.FATTR4_FILES_FREE:
                fsStat = getFsStat(fsStat, fs);
                fattr4_files_free files_free = new fattr4_files_free(new uint64_t(fsStat.getTotalFiles() - fsStat.getUsedFiles()));
                return Optional.of(files_free);
            case nfs4_prot.FATTR4_FILES_TOTAL:
                fsStat = getFsStat(fsStat, fs);
                return Optional.of(new fattr4_files_total(new uint64_t(fsStat.getTotalFiles())));
            case nfs4_prot.FATTR4_FS_LOCATIONS:
                return Optional.absent();
            case nfs4_prot.FATTR4_HIDDEN:
                return Optional.of(new fattr4_hidden(false));
            case nfs4_prot.FATTR4_HOMOGENEOUS:
                return Optional.of(new fattr4_homogeneous(true));
            case nfs4_prot.FATTR4_MAXFILESIZE:
                return Optional.of(new fattr4_maxfilesize(new uint64_t(NFSv4Defaults.NFS4_MAXFILESIZE)));
            case nfs4_prot.FATTR4_MAXLINK:
                return Optional.of(new fattr4_maxlink(new uint32_t(NFSv4Defaults.NFS4_MAXLINK)));
            case nfs4_prot.FATTR4_MAXNAME:
                return Optional.of(new fattr4_maxname(new uint32_t(NFSv4Defaults.NFS4_MAXFILENAME)));
            case nfs4_prot.FATTR4_MAXREAD:
                return Optional.of(new fattr4_maxread(new uint64_t(NFSv4Defaults.NFS4_MAXIOBUFFERSIZE)));
            case nfs4_prot.FATTR4_MAXWRITE:
                fattr4_maxwrite maxwrite = new fattr4_maxwrite(new uint64_t(NFSv4Defaults.NFS4_MAXIOBUFFERSIZE));
                return Optional.of(maxwrite);
            case nfs4_prot.FATTR4_MIMETYPE:
                return Optional.absent();
            case nfs4_prot.FATTR4_MODE:
                mode4 fmode = new mode4();
                fmode.value = new uint32_t(stat.getMode() & 07777);
                return Optional.of(new fattr4_mode(fmode));
            case nfs4_prot.FATTR4_NO_TRUNC:
                return Optional.of(new fattr4_no_trunc(true));
            case nfs4_prot.FATTR4_NUMLINKS:
                uint32_t nlinks = new uint32_t(stat.getNlink());
                return Optional.of(new fattr4_numlinks(nlinks));
            case nfs4_prot.FATTR4_OWNER:
                String owner_s = context.getIdMapping().uidToPrincipal(stat.getUid());
                utf8str_mixed user = new utf8str_mixed(owner_s);
                return Optional.of(new fattr4_owner(user));
            case nfs4_prot.FATTR4_OWNER_GROUP:
                String group_s = context.getIdMapping().gidToPrincipal(stat.getGid());
                utf8str_mixed group = new utf8str_mixed(group_s);
                return Optional.of(new fattr4_owner(group));
            case nfs4_prot.FATTR4_QUOTA_AVAIL_HARD:
                return Optional.absent();
            case nfs4_prot.FATTR4_QUOTA_AVAIL_SOFT:
                return Optional.absent();
            case nfs4_prot.FATTR4_QUOTA_USED:
                return Optional.absent();
            case nfs4_prot.FATTR4_RAWDEV:
                specdata4 dev = new specdata4();
                dev.specdata1 = new uint32_t(0);
                dev.specdata2 = new uint32_t(0);
                return Optional.of(new fattr4_rawdev(dev));
            case nfs4_prot.FATTR4_SPACE_AVAIL:
                fsStat = getFsStat(fsStat, fs);
                uint64_t spaceAvail = new uint64_t(fsStat.getTotalSpace() - fsStat.getUsedSpace());
                return Optional.of(spaceAvail);
            case nfs4_prot.FATTR4_SPACE_FREE:
                fsStat = getFsStat(fsStat, fs);
                fattr4_space_free space_free = new fattr4_space_free(new uint64_t(fsStat.getTotalSpace() - fsStat.getUsedSpace()));
                return Optional.of(space_free);
            case nfs4_prot.FATTR4_SPACE_TOTAL:
                fsStat = getFsStat(fsStat, fs);
                return Optional.of(new fattr4_space_total(new uint64_t(fsStat.getTotalSpace())));
            case nfs4_prot.FATTR4_SPACE_USED:
                return Optional.of(new fattr4_space_used(new uint64_t(stat.getSize())));
            case nfs4_prot.FATTR4_SYSTEM:
                return Optional.of(new fattr4_system(false));
            case nfs4_prot.FATTR4_TIME_ACCESS:
                nfstime4 atime = new nfstime4();
                atime.seconds = new int64_t(TimeUnit.SECONDS.convert(stat.getATime(), TimeUnit.MILLISECONDS));
                atime.nseconds = new uint32_t(0);
                return Optional.of(new fattr4_time_access(atime));
            case nfs4_prot.FATTR4_TIME_BACKUP:
                return Optional.absent();
            case nfs4_prot.FATTR4_TIME_CREATE:
                nfstime4 ctime = new nfstime4();
                ctime.seconds = new int64_t(TimeUnit.SECONDS.convert(stat.getCTime(), TimeUnit.MILLISECONDS));
                ctime.nseconds = new uint32_t(0);
                return Optional.of(new fattr4_time_create(ctime));
            case nfs4_prot.FATTR4_TIME_DELTA:
                return Optional.absent();
            case nfs4_prot.FATTR4_TIME_METADATA:
                nfstime4 mdtime = new nfstime4();
                mdtime.seconds = new int64_t(TimeUnit.SECONDS.convert(stat.getCTime(), TimeUnit.MILLISECONDS));
                mdtime.nseconds = new uint32_t(0);
                return Optional.of(new fattr4_time_metadata(mdtime));
            case nfs4_prot.FATTR4_TIME_MODIFY:
                nfstime4 mtime = new nfstime4();
                mtime.seconds = new int64_t(TimeUnit.SECONDS.convert(stat.getMTime(), TimeUnit.MILLISECONDS));
                mtime.nseconds = new uint32_t(0);
                return Optional.of(new fattr4_time_modify(mtime));
            case nfs4_prot.FATTR4_MOUNTED_ON_FILEID:

                /*
                 * TODO!!!:
                 */

                long mofi = stat.getFileId();

                if (mofi == 0x00b0a23a /* it's a root*/) {
                    mofi = 0x12345678;
                }

                uint64_t rootid = new uint64_t(mofi);
                fattr4_mounted_on_fileid mounted_on_fileid = new fattr4_mounted_on_fileid(rootid);
                return Optional.of(mounted_on_fileid);

            /**
             * this is NFSv4.1 (pNFS) specific code, which is still in the
             * development ( as protocol )
             */
            case nfs4_prot.FATTR4_FS_LAYOUT_TYPE:
                fattr4_fs_layout_types fs_layout_type = new fattr4_fs_layout_types();
                fs_layout_type.value = new int[1];
                fs_layout_type.value[0] = layouttype4.LAYOUT4_NFSV4_1_FILES;
                return Optional.of(fs_layout_type);
            case nfs4_prot.FATTR4_TIME_MODIFY_SET:
            case nfs4_prot.FATTR4_TIME_ACCESS_SET:
                throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "getattr of write-only attributes");
            default:
                _log.debug("GETATTR for #{}", fattr);
                return Optional.absent();
        }
    }


	public static String attrMask2String( int offset ) {

        String maskName = "Unknown";

        switch(offset) {

            case nfs4_prot.FATTR4_SUPPORTED_ATTRS :
                maskName=" FATTR4_SUPPORTED_ATTRS ";
                break;
            case nfs4_prot.FATTR4_TYPE :
                maskName=" FATTR4_TYPE ";
                break;
            case nfs4_prot.FATTR4_FH_EXPIRE_TYPE :
                maskName=" FATTR4_FH_EXPIRE_TYPE ";
                break;
            case nfs4_prot.FATTR4_CHANGE :
                maskName=" FATTR4_CHANGE ";
                break;
            case nfs4_prot.FATTR4_SIZE :
                maskName=" FATTR4_SIZE ";
                break;
            case nfs4_prot.FATTR4_LINK_SUPPORT :
                maskName=" FATTR4_LINK_SUPPORT ";
                break;
            case nfs4_prot.FATTR4_SYMLINK_SUPPORT :
                maskName=" FATTR4_SYMLINK_SUPPORT ";
                break;
            case nfs4_prot.FATTR4_NAMED_ATTR :
                maskName=" FATTR4_NAMED_ATTR ";
                break;
            case nfs4_prot.FATTR4_FSID :
                maskName=" FATTR4_FSID ";
                break;
            case nfs4_prot.FATTR4_UNIQUE_HANDLES :
                maskName=" FATTR4_UNIQUE_HANDLES ";
                break;
            case nfs4_prot.FATTR4_LEASE_TIME :
                maskName=" FATTR4_LEASE_TIME ";
                break;
            case nfs4_prot.FATTR4_RDATTR_ERROR :
                maskName=" FATTR4_RDATTR_ERROR ";
                break;
            case nfs4_prot.FATTR4_FILEHANDLE :
                maskName=" FATTR4_FILEHANDLE ";
                break;
            case nfs4_prot.FATTR4_ACL :
                maskName=" FATTR4_ACL ";
                break;
            case nfs4_prot.FATTR4_ACLSUPPORT :
                maskName=" FATTR4_ACLSUPPORT ";
                break;
            case nfs4_prot.FATTR4_ARCHIVE :
                maskName=" FATTR4_ARCHIVE ";
                break;
            case nfs4_prot.FATTR4_CANSETTIME :
                maskName=" FATTR4_CANSETTIME ";
                break;
            case nfs4_prot.FATTR4_CASE_INSENSITIVE :
                maskName=" FATTR4_CASE_INSENSITIVE ";
                break;
            case nfs4_prot.FATTR4_CASE_PRESERVING :
                maskName=" FATTR4_CASE_PRESERVING ";
                break;
            case nfs4_prot.FATTR4_CHOWN_RESTRICTED :
                maskName=" FATTR4_CHOWN_RESTRICTED ";
                break;
            case nfs4_prot.FATTR4_FILEID :
                maskName=" FATTR4_FILEID ";
                break;
            case nfs4_prot.FATTR4_FILES_AVAIL :
                maskName=" FATTR4_FILES_AVAIL ";
                break;
            case nfs4_prot.FATTR4_FILES_FREE :
                maskName=" FATTR4_FILES_FREE ";
                break;
            case nfs4_prot.FATTR4_FILES_TOTAL :
                maskName=" FATTR4_FILES_TOTAL ";
                break;
            case nfs4_prot.FATTR4_FS_LOCATIONS :
                maskName=" FATTR4_FS_LOCATIONS ";
                break;
            case nfs4_prot.FATTR4_HIDDEN :
                maskName=" FATTR4_HIDDEN ";
                break;
            case nfs4_prot.FATTR4_HOMOGENEOUS :
                maskName=" FATTR4_HOMOGENEOUS ";
                break;
            case nfs4_prot.FATTR4_MAXFILESIZE :
                maskName=" FATTR4_MAXFILESIZE ";
                break;
            case nfs4_prot.FATTR4_MAXLINK :
                maskName=" FATTR4_MAXLINK ";
                break;
            case nfs4_prot.FATTR4_MAXNAME :
                maskName=" FATTR4_MAXNAME ";
                break;
            case nfs4_prot.FATTR4_MAXREAD :
                maskName=" FATTR4_MAXREAD ";
                break;
            case nfs4_prot.FATTR4_MAXWRITE :
                maskName=" FATTR4_MAXWRITE ";
                break;
            case nfs4_prot.FATTR4_MIMETYPE :
                maskName=" FATTR4_MIMETYPE ";
                break;
            case nfs4_prot.FATTR4_MODE :
                maskName=" FATTR4_MODE ";
                break;
            case nfs4_prot.FATTR4_NO_TRUNC :
                maskName=" FATTR4_NO_TRUNC ";
                break;
            case nfs4_prot.FATTR4_NUMLINKS :
                maskName=" FATTR4_NUMLINKS ";
                break;
            case nfs4_prot.FATTR4_OWNER :
                maskName=" FATTR4_OWNER ";
                break;
            case nfs4_prot.FATTR4_OWNER_GROUP :
                maskName=" FATTR4_OWNER_GROUP ";
                break;
            case nfs4_prot.FATTR4_QUOTA_AVAIL_HARD :
                maskName=" FATTR4_QUOTA_AVAIL_HARD ";
                break;
            case nfs4_prot.FATTR4_QUOTA_AVAIL_SOFT :
                maskName=" FATTR4_QUOTA_AVAIL_SOFT ";
                break;
            case nfs4_prot.FATTR4_QUOTA_USED :
                maskName=" FATTR4_QUOTA_USED ";
                break;
            case nfs4_prot.FATTR4_RAWDEV :
                maskName=" FATTR4_RAWDEV ";
                break;
            case nfs4_prot.FATTR4_SPACE_AVAIL :
                maskName=" FATTR4_SPACE_AVAIL ";
                break;
            case nfs4_prot.FATTR4_SPACE_FREE :
                maskName=" FATTR4_SPACE_FREE ";
                break;
            case nfs4_prot.FATTR4_SPACE_TOTAL :
                maskName=" FATTR4_SPACE_TOTAL ";
                break;
            case nfs4_prot.FATTR4_SPACE_USED :
                maskName=" FATTR4_SPACE_USED ";
                break;
            case nfs4_prot.FATTR4_SYSTEM :
                maskName=" FATTR4_SYSTEM ";
                break;
            case nfs4_prot.FATTR4_TIME_ACCESS :
                maskName=" FATTR4_TIME_ACCESS ";
                break;
            case nfs4_prot.FATTR4_TIME_ACCESS_SET :
                maskName=" FATTR4_TIME_ACCESS_SET ";
                break;
            case nfs4_prot.FATTR4_TIME_BACKUP :
                maskName=" FATTR4_TIME_BACKUP ";
                break;
            case nfs4_prot.FATTR4_TIME_CREATE :
                maskName=" FATTR4_TIME_CREATE ";
                break;
            case nfs4_prot.FATTR4_TIME_DELTA :
                maskName=" FATTR4_TIME_DELTA ";
                break;
            case nfs4_prot.FATTR4_TIME_METADATA :
                maskName=" FATTR4_TIME_METADATA ";
                break;
            case nfs4_prot.FATTR4_TIME_MODIFY :
                maskName=" FATTR4_TIME_MODIFY ";
                break;
            case nfs4_prot.FATTR4_TIME_MODIFY_SET :
                maskName=" FATTR4_TIME_MODIFY_SET ";
                break;
            case nfs4_prot.FATTR4_MOUNTED_ON_FILEID :
                maskName=" FATTR4_MOUNTED_ON_FILEID ";
                break;
            case nfs4_prot.FATTR4_FS_LAYOUT_TYPE :
                maskName=" FATTR4_FS_LAYOUT_TYPE ";
                break;
            case nfs4_prot.FATTR4_LAYOUT_HINT:
                maskName=" FATTR4_LAYOUT_HINT ";
                break;
            case nfs4_prot.FATTR4_LAYOUT_TYPE:
                maskName=" FATTR4_LAYOUT_TYPE ";
                break;
            case nfs4_prot.FATTR4_LAYOUT_BLKSIZE:
                maskName=" FATTR4_LAYOUT_BLKSIZE ";
                break;
            case nfs4_prot.FATTR4_LAYOUT_ALIGNMENT:
                maskName=" FATTR4_LAYOUT_ALIGNMENT ";
                break;
            case nfs4_prot.FATTR4_FS_LOCATIONS_INFO:
                maskName=" FATTR4_FS_LOCATIONS_INFO ";
                break;
            case nfs4_prot.FATTR4_MDSTHRESHOLD:
                maskName=" FATTR4_MDSTHRESHOLD ";
                break;
            case nfs4_prot.FATTR4_RETENTION_GET:
                maskName=" FATTR4_RETENTION_GET ";
                break;
            case nfs4_prot.FATTR4_RETENTION_SET:
                maskName=" FATTR4_RETENTION_SET ";
                break;
            case nfs4_prot.FATTR4_RETENTEVT_GET:
                maskName=" FATTR4_RETENTEVT_GET ";
                break;
            case nfs4_prot.FATTR4_RETENTEVT_SET:
                maskName=" FATTR4_RETENTEVT_SET ";
                break;
            case nfs4_prot.FATTR4_RETENTION_HOLD:
                maskName=" FATTR4_RETENTION_HOLD ";
                break;
            case nfs4_prot.FATTR4_MODE_SET_MASKED:
                maskName=" FATTR4_MODE_SET_MASKED ";
                break;
            case nfs4_prot.FATTR4_FS_CHARSET_CAP:
                maskName=" FATTR4_FS_CHARSET_CAP ";
                break;
            default:
            	maskName += "(" + offset + ")";
        }

        return maskName;

    }


    static int unixType2NFS( int type ) {

        int ret = 0;

        int mask =  0770000;

        switch ( type & mask  ) {

            case UnixPermission.S_IFREG:
                ret = nfs_ftype4.NF4REG;
                break;
            case UnixPermission.S_IFDIR:
                ret = nfs_ftype4.NF4DIR;
                break;
            case UnixPermission.S_IFLNK:
                ret = nfs_ftype4.NF4LNK;
                break;
            case UnixPermission.S_IFSOCK:
                ret = nfs_ftype4.NF4SOCK;
                break;
            case UnixPermission.S_IFBLK:
                ret = nfs_ftype4.NF4BLK;
                break;
            case UnixPermission.S_IFCHR:
                ret = nfs_ftype4.NF4CHR;
                break;
            case UnixPermission.S_IFIFO:
                ret = nfs_ftype4.NF4FIFO;
                break;
            default:
                _log.info("Unknown mode [{}]",  Integer.toOctalString(type));
                ret = 0;

        }

        return ret;
    }

}
