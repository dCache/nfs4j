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

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationDELEGRETURN extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationDELEGRETURN.class);

    public OperationDELEGRETURN(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_DELEGRETURN);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        final Inode inode = context.currentInode();
        stateid4 stateid = Stateids.getCurrentStateidIfNeeded(context, _args.opdelegreturn.deleg_stateid);
        NFS4Client client;
        if (context.getMinorversion() > 0) {
            client = context.getSession().getClient();
        } else {
            client = context.getStateHandler().getClientIdByStateId(stateid);
        }

        context.getStateHandler().getFileTracker().delegationReturn(client, stateid, inode);
        result.opdelegreturn.status = nfsstat.NFS_OK;
    }
}
