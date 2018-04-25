/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import org.dcache.nfs.v3.xdr.exportnode;
import org.dcache.nfs.v3.xdr.mountbody;
import org.dcache.nfs.v3.xdr.fhandle3;
import org.dcache.nfs.v3.xdr.mountres3;
import org.dcache.nfs.v3.xdr.name;
import org.dcache.nfs.v3.xdr.exports;
import org.dcache.nfs.v3.xdr.mount_protServerStub;
import org.dcache.nfs.v3.xdr.fhstatus;
import org.dcache.nfs.v3.xdr.groups;
import org.dcache.nfs.v3.xdr.dirpath;
import org.dcache.nfs.v3.xdr.mountlist;
import org.dcache.nfs.v3.xdr.groupnode;
import org.dcache.nfs.v3.xdr.mountres3_ok;
import org.dcache.nfs.v3.xdr.mountstat3;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.status.*;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.PseudoFs;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.oncrpc4j.rpc.RpcAuthType;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MountServer extends mount_protServerStub {

    private static final Logger _log = LoggerFactory.getLogger(MountServer.class);
    private final ExportFile _exportFile;
    private final Multimap<String, InetAddress> _mounts = HashMultimap.create();
    private final VirtualFileSystem _vfs;

    /*
     * pseudo flavors as defined in RFC2623
     */
    public final static int RPC_AUTH_GSS_KRB5 = 390003;
    public final static int RPC_AUTH_GSS_KRB5I = 390004;
    public final static int RPC_AUTH_GSS_KRB5P = 390005;

    public MountServer(ExportFile exportFile, VirtualFileSystem fs) {
        super();
        _exportFile = exportFile;
        _vfs = fs;
    }

    @Override
    public void MOUNTPROC3_NULL_3(RpcCall call$) {
        // NOP
    }

    @Override
    public mountres3 MOUNTPROC3_MNT_3(RpcCall call$, dirpath arg1) {

        mountres3 m = new mountres3();

        java.io.File f = new java.io.File(arg1.value);
        String mountPoint = f.getAbsolutePath();
        InetAddress remoteAddress = call$.getTransport().getRemoteSocketAddress().getAddress();
        _log.debug("Mount request for: {}", mountPoint);

        FsExport export = _exportFile.getExport(mountPoint, remoteAddress);
        if (export == null) {
            m.fhs_status = mountstat3.MNT3ERR_ACCES;
            _log.info("Mount deny for: {}:{}", remoteAddress, mountPoint);
            return m;
        }

        m.mountinfo = new mountres3_ok();

        try {

            Inode rootInode = path2Inode(_vfs, mountPoint);
            Stat stat = _vfs.getattr(rootInode);

            if (stat.type() == Stat.Type.SYMLINK) {
                /*
                 * we resolve symlink only once
                 */
                String path = _vfs.readlink(rootInode);
                rootInode = path2Inode(_vfs, path);
                stat = _vfs.getattr(rootInode);
            }

            if (stat.type() != Stat.Type.DIRECTORY) {
                throw new NotDirException("Path is not a directory");
            }

            byte[] b = PseudoFs.pseudoIdToReal(rootInode, export.getIndex()).toNfsHandle();

            m.fhs_status = mountstat3.MNT3_OK;
            m.mountinfo.fhandle = new fhandle3(b);
            m.mountinfo.auth_flavors = exportSecFlavors(export);

            _mounts.put(mountPoint, remoteAddress);

        } catch (ChimeraNFSException e) {
            _log.warn("mount request failed: ", e.getMessage());
            m.fhs_status = e.getStatus();
        } catch (IOException e) {
            m.fhs_status = mountstat3.MNT3ERR_SERVERFAULT;
        }

        return m;

    }

    @Override
    public mountlist MOUNTPROC3_DUMP_3(RpcCall call$) {

        mountlist mFullList = new mountlist();
        mountlist mList = mFullList;
        mList.value = null;

        for (Map.Entry<String, InetAddress> mountEntry : _mounts.entries()) {
            String path = mountEntry.getKey();
            InetAddress remoteAddress = mountEntry.getValue();

            mList.value = new mountbody();
            mList.value.ml_directory = new dirpath(path);
            mList.value.ml_hostname = new name(remoteAddress.getHostName());
            mList.value.ml_next = new mountlist();
            mList.value.ml_next.value = null;
            mList = mList.value.ml_next;
        }

        return mFullList;
    }

    @Override
    public void MOUNTPROC3_UMNT_3(RpcCall call$, dirpath arg1) {
        _mounts.remove(arg1.value, call$.getTransport().getRemoteSocketAddress().getHostName());
    }

    @Override
    public void MOUNTPROC3_UMNTALL_3(RpcCall call$) {
    }

    @Override
    public exports MOUNTPROC3_EXPORT_3(RpcCall call$) {

        exports eFullList = new exports();
        exports eList = eFullList;

        eList.value = null;

        Map<String, List<FsExport>> exports = _exportFile
                .getExports()
                .collect(Collectors.groupingBy(FsExport::getPath));

        for (String path : exports.keySet()) {

            eList.value = new exportnode();
            eList.value.ex_dir = new dirpath(path);
            eList.value.ex_groups = new groups();
            eList.value.ex_groups.value = null;
            groups g = eList.value.ex_groups;

            for (FsExport export : exports.get(path)) {

                g.value = new groupnode();
                g.value.gr_name = new name(export.client());
                g.value.gr_next = new groups();
                g.value.gr_next.value = null;

                g = g.value.gr_next;
            }

            eList.value.ex_next = new exports();
            eList.value.ex_next.value = null;
            eList = eList.value.ex_next;

        }
        return eFullList;
    }


    /*
     * MOUNT version 1 support for exports, umount and so on
     */
    @Override
    public void MOUNTPROC_NULL_1(RpcCall call$) {
        // ping-pong
    }

    @Override
    public fhstatus MOUNTPROC_MNT_1(RpcCall call$, dirpath arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public mountlist MOUNTPROC_DUMP_1(RpcCall call$) {
        // Same as V3
        return this.MOUNTPROC3_DUMP_3(call$);
    }

    @Override
    public void MOUNTPROC_UMNT_1(RpcCall call$, dirpath arg1) {
        // same as v3
        this.MOUNTPROC3_UMNT_3(call$, arg1);
    }

    @Override
    public void MOUNTPROC_UMNTALL_1(RpcCall call$) {
        // TODO Auto-generated method stub
    }

    @Override
    public exports MOUNTPROC_EXPORT_1(RpcCall call$) {
        // Same as V3
        return this.MOUNTPROC3_EXPORT_3(call$);
    }

    @Override
    public exports MOUNTPROC_EXPORTALL_1(RpcCall call$) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Inode path2Inode(VirtualFileSystem fs, String path)
            throws ChimeraNFSException, IOException {

        Splitter splitter = Splitter.on('/').omitEmptyStrings();
        Inode inode = fs.getRootInode();

        for (String pathElement : splitter.split(path)) {
            inode = fs.lookup(inode, pathElement);
        }
        return inode;
    }

    private int[] exportSecFlavors(FsExport export) throws ChimeraNFSException {
        FsExport.Sec sec = export.getSec();
        int[] supportedFlavors;
        switch(sec) {
            case KRB5:
                supportedFlavors = new int[]{RPC_AUTH_GSS_KRB5, RPC_AUTH_GSS_KRB5I, RPC_AUTH_GSS_KRB5P};
                break;
            case KRB5I:
                supportedFlavors = new int[]{RPC_AUTH_GSS_KRB5I, RPC_AUTH_GSS_KRB5P};
                break;
            case KRB5P:
                supportedFlavors = new int[]{RPC_AUTH_GSS_KRB5P};
                break;
            case SYS:
                supportedFlavors = new int[]{RPC_AUTH_GSS_KRB5, RPC_AUTH_GSS_KRB5I, RPC_AUTH_GSS_KRB5P, RpcAuthType.UNIX};
                break;
            case NONE:
                supportedFlavors = new int[]{RPC_AUTH_GSS_KRB5, RPC_AUTH_GSS_KRB5I, RPC_AUTH_GSS_KRB5P, RpcAuthType.UNIX, RpcAuthType.NONE};
                break;
            default:
                // shuold never happen
                throw new PermException("Unsupported secutiry flavor");
        }
        return supportedFlavors;
    }
}
