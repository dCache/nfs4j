/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.LAYOUTGET4resok;
import java.io.IOException;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.BadIoModeException;
import org.dcache.nfs.status.BadLayoutException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.LayoutUnavailableException;
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

        /*
         * dCache supports FILE layout
         *
         * LAYOUTGET is used by a client to get a layout for a file
         */

        /*
         * DRAFT-17: To get a layout from a specific offset through the
         * end-of-file, regardless of the file's length, a loga_length field
         * with all bits set to 1 (one) should be used
         */
        length4 lengthRange = new length4(0xffffffff);

        if (_args.oplayoutget.loga_offset.value != 0) {
            if (_args.oplayoutget.loga_length.value == 0) {
                throw new InvalException("length == 0");
            }

            /*
             * FIXME: sing/unsign issue here
             */
//            if ((_args.oplayoutget.loga_length.value.value + _args.oplayoutget.loga_offset.value.value) > lengthRange.value.value) {
//                throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "offset+length too big");
//            }
        }

        if (!(_args.oplayoutget.loga_iomode == layoutiomode4.LAYOUTIOMODE4_RW
                || _args.oplayoutget.loga_iomode == layoutiomode4.LAYOUTIOMODE4_READ)) {
            throw new BadIoModeException("invalid loga_iomode");
        }

        if (_args.oplayoutget.loga_layout_type > 3) {
            throw new BadLayoutException("layouts supported but no matching found (" + _args.oplayoutget.loga_layout_type + ")");
        }

        /*
         * FIXME: sing/unsign issue here
         */
//        if (_args.oplayoutget.loga_minlength.value.value < 1) {
//               throw new ChimeraNFSException(nfsstat.NFSERR_BADLAYOUT, "loga_minlength field should be at least one.");
//        }

        Inode inode = context.currentInode();

        if (!isPnfsAllowed(context, inode)) {
            throw new LayoutUnavailableException("pNFS is not allowed");
        }

        Layout ioLayout = context.getDeviceManager().layoutGet(context, inode,
                _args.oplayoutget.loga_layout_type,
                _args.oplayoutget.loga_iomode,
                _args.oplayoutget.loga_stateid);

        res.logr_resok4 = new LAYOUTGET4resok();
        res.logr_resok4.logr_layout = ioLayout.getLayoutSegments();
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
