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

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.v4.xdr.OPEN_DOWNGRADE4res;
import org.dcache.nfs.v4.xdr.OPEN_DOWNGRADE4resok;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOPEN_DOWNGRADE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationOPEN_DOWNGRADE.class);

    public OperationOPEN_DOWNGRADE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN_DOWNGRADE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        final OPEN_DOWNGRADE4res res = result.opopen_downgrade;

        final int shareAccess = _args.opopen_downgrade.share_access.value & ~nfs4_prot.OPEN4_SHARE_ACCESS_WANT_DELEG_MASK;
        final int shareDeny = _args.opopen_downgrade.share_deny.value & ~nfs4_prot.OPEN4_SHARE_ACCESS_WANT_DELEG_MASK;

        /*
         * Share access must be one of OPEN4_SHARE_ACCESS_READ, OPEN4_SHARE_ACCESS_WRITE
         * or OPEN4_SHARE_ACCESS_BOTH. Share deny can be zero or one of
         * OPEN4_SHARE_DENY_READ, OPEN4_SHARE_DENY_WRITE or OPEN4_SHARE_DENY_BOTH.
         */
        if ((shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_BOTH) == 0) {
            throw new InvalException("Invalid share access mode");
        }

        if ((shareAccess & ~nfs4_prot.OPEN4_SHARE_ACCESS_BOTH) != 0) {
            throw new InvalException("Invalid share access mode");
        }

        if ((shareDeny & ~nfs4_prot.OPEN4_SHARE_DENY_BOTH) != 0) {
            throw new InvalException("Invalid share deny mode");
        }

        final Inode inode = context.currentInode();
        stateid4 stateid = Stateids.getCurrentStateidIfNeeded(context, _args.opopen_downgrade.open_stateid);
        NFS4Client client;
        if (context.getMinorversion() > 0) {
            client = context.getSession().getClient();
        } else {
            client = context.getStateHandler().getClientIdByStateId(stateid);
        }

        NFS4State nfsState = client.state(stateid);
        Stateids.checkStateId(nfsState.stateid(), stateid);

        if (context.getMinorversion() == 0) {
            nfsState.getStateOwner().acceptAsNextSequence(_args.opopen_downgrade.seqid);
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new OPEN_DOWNGRADE4resok();
        res.resok4.open_stateid = context.getStateHandler()
                .getFileTracker()
                .downgradeOpen(client, stateid, inode, shareAccess, shareDeny);
    }
}
