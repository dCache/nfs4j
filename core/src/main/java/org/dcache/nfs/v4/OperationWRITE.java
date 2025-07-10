/*
 * Copyright (c) 2009 - 2025 Deutsches Elektronen-Synchroton,
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

import java.io.IOException;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.OpenModeException;
import org.dcache.nfs.v4.xdr.WRITE4res;
import org.dcache.nfs.v4.xdr.WRITE4resok;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationWRITE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationWRITE.class);

    public OperationWRITE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_WRITE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        final WRITE4res res = result.opwrite;

        _args.opwrite.offset.checkOverflow(_args.opwrite.data.remaining(), "offset + length overflow");

        Stat.Type statType = context.getFs().getattr(context.currentInode(), Stat.STAT_ATTRIBUTES_TYPE_ONLY).type();
        stateid4 stateid = Stateids.getCurrentStateidIfNeeded(context, _args.opwrite.stateid);

        if (statType == Stat.Type.DIRECTORY) {
            throw new IsDirException();
        }

        if (statType == Stat.Type.SYMLINK) {
            throw new InvalException("path is a symlink");
        }

        var inode = context.currentInode();
        if (Stateids.isStateLess(stateid)) {
            // Anonymous access as per RFC 7530
            // https://datatracker.ietf.org/doc/html/rfc7530#section-9.1.4.3
            // we only check file access rights.
            if (context.getFs().access(context.getSubject(), inode, nfs4_prot.ACCESS4_MODIFY) == 0) {
                throw new AccessException();
            }

        } else {

            NFS4Client client;
            if (context.getMinorversion() == 0) {
                /*
                 * The NFSv4.0 spec requires lease renewal on WRITE. See: https://tools.ietf.org/html/rfc7530#page-119
                 *
                 * With introduction of sessions in v4.1 update of the lease time done through SEQUENCE operations.
                 */
                context.getStateHandler().updateClientLeaseTime(stateid);
                client = context.getStateHandler().getClientIdByStateId(stateid);
            } else {
                client = context.getSession().getClient();
            }

            int shareAccess = context.getStateHandler().getFileTracker().getShareAccess(client, inode, stateid);
            if ((shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) == 0) {
                throw new OpenModeException("Invalid open mode");
            }
        }

        long offset = _args.opwrite.offset.value;
        VirtualFileSystem.WriteResult writeResult = context.getFs().write(context.currentInode(),
                _args.opwrite.data, offset, VirtualFileSystem.StabilityLevel.fromStableHow(_args.opwrite.stable));

        if (writeResult.getBytesWritten() < 0) {
            throw new NfsIoException("IO not allowed");
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new WRITE4resok();
        res.resok4.count = new count4(writeResult.getBytesWritten());
        res.resok4.committed = writeResult.getStabilityLevel().toStableHow();
        res.resok4.writeverf = context.getRebootVerifier();

    }
}
