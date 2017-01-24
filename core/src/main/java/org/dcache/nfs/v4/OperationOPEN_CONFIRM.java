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

import java.io.IOException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.OPEN_CONFIRM4resok;
import org.dcache.nfs.v4.xdr.OPEN_CONFIRM4res;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOPEN_CONFIRM extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationOPEN_CONFIRM.class);

    public OperationOPEN_CONFIRM(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN_CONFIRM);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final OPEN_CONFIRM4res res = result.opopen_confirm;

        if (context.getMinorversion() > 0) {
            throw new NotSuppException("operation OPEN_CONFIRM4 is obsolete in 4.x, x > 0");
        }

        Inode inode = context.currentInode();
        Stat stat = context.getFs().getattr(context.currentInode());

        if (stat.type() == Stat.Type.DIRECTORY) {
            throw new IsDirException();
        }

        if (stat.type() == Stat.Type.SYMLINK) {
            throw new InvalException();
        }

        stateid4 stateid = _args.opopen_confirm.open_stateid;
        _log.debug("confirmed stateID: {}", stateid);

        NFS4Client client = context.getStateHandler().getClientIdByStateId(stateid);
        NFS4State state = client.state(stateid);
        state.getStateOwner().acceptAsNextSequence(_args.opopen_confirm.seqid);

        state.bumpSeqid();
        state.confirm();

        res.resok4 = new OPEN_CONFIRM4resok();
        res.resok4.open_stateid = state.stateid();

        res.status = nfsstat.NFS_OK;
    }
}
