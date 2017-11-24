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
package org.dcache.nfs.v4;

import org.dcache.nfs.v4.xdr.layoutiomode4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.LAYOUTGET4res;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.LAYOUTGET4resok;
import java.io.IOException;
import java.util.Arrays;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.BadIoModeException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.LayoutUnavailableException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.OpenModeException;
import org.dcache.nfs.status.TooSmallException;
import org.dcache.nfs.v4.xdr.layout4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLAYOUTGET extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationLAYOUTGET.class);

    public OperationLAYOUTGET(nfs_argop4 args) {
    super(args, nfs_opnum4.OP_LAYOUTGET);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final LAYOUTGET4res res = result.oplayoutget;
        final NFSv41DeviceManager pnfsDeviceManager = context
                .getDeviceManager()
                .orElseThrow(() -> new NotSuppException("pNFS device manager not configured"));

        if ((_args.oplayoutget.loga_length.value != nfs4_prot.NFS4_UINT64_MAX) &&
                (_args.oplayoutget.loga_length.value < _args.oplayoutget.loga_minlength.value)) {
            throw new InvalException("requested layout length is smaller than minlen");
        }

        if (_args.oplayoutget.loga_length.value != nfs4_prot.NFS4_UINT64_MAX) {
            _args.oplayoutget.loga_offset.checkOverflow(_args.oplayoutget.loga_length, "offset + length overflow");
        }

        if (_args.oplayoutget.loga_minlength.value != nfs4_prot.NFS4_UINT64_MAX) {
            _args.oplayoutget.loga_offset.checkOverflow(_args.oplayoutget.loga_minlength, "offset + minlength overflow");
        }

        if (!(_args.oplayoutget.loga_iomode == layoutiomode4.LAYOUTIOMODE4_RW
                || _args.oplayoutget.loga_iomode == layoutiomode4.LAYOUTIOMODE4_READ)) {
            throw new BadIoModeException("invalid loga_iomode");
        }

        layouttype4 layoutType = layouttype4.valueOf(_args.oplayoutget.loga_layout_type);
        Inode inode = context.currentInode();

        if (!isPnfsAllowed(context, inode)) {
            throw new LayoutUnavailableException("pNFS is not allowed");
        }

        int ioMode = _args.oplayoutget.loga_iomode;

        NFS4State state = context.getSession().getClient().state(_args.oplayoutget.loga_stateid);
        // check open file mode
        int shareAccess = context
                .getStateHandler()
                .getFileTracker()
                .getShareAccess(context.getSession().getClient(), inode, state.getOpenState().stateid());

        if ((shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) == 0 &&
                (ioMode & layoutiomode4.LAYOUTIOMODE4_RW) != 0) {
            throw new OpenModeException("can't provide RW layout for RO open");
        }

        Layout ioLayout;
        try {
            ioLayout = pnfsDeviceManager.layoutGet(context, inode,
                    layoutType,
                    _args.oplayoutget.loga_iomode,
                    _args.oplayoutget.loga_stateid);
        } catch (NfsIoException e) {
            // linux client can't handle EIO on layout get. force it to proxy IO to
            // hit a different code path.
            throw new LayoutUnavailableException("IO error on layout get", e);
        }

        layout4[] layoutSegments = ioLayout.getLayoutSegments();

        int n = 0;
        /*
         count how many segments client can accept
        */
        int layoutBodySize = 0;
        for(layout4 layout: layoutSegments) {
            layoutBodySize += layout.lo_content.loc_body.length;
            if ( layoutBodySize > _args.oplayoutget.loga_maxcount.value) {
                break;
            }
            n++;
        }

        if (n == 0) {
            /*
              there is no room even for one segment
            */
            throw new TooSmallException("layout body size is bigger than client can accept");
        }

        /*
          truncate number of segments if needed;
        */
        layoutSegments = n == layoutSegments.length ? layoutSegments : Arrays.copyOf(layoutSegments, n);

        res.logr_resok4 = new LAYOUTGET4resok();
        res.logr_resok4.logr_layout = layoutSegments;
        res.logr_resok4.logr_stateid = ioLayout.getStateid();
        res.logr_resok4.logr_return_on_close = ioLayout.returnOnClose();

        res.logr_status = nfsstat.NFS_OK;
    }

    private boolean isPnfsAllowed(CompoundContext context, Inode inode) throws ChimeraNFSException {
        FsExport export = context
                .getExportFile()
                .getExport(inode.exportIndex(), context.getRemoteSocketAddress().getAddress());
        if (export == null) {
            throw new AccessException("no export");
        }

        return export.isWithPnfs();
    }
}
