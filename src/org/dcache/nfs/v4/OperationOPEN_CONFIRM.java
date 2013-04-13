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

import java.io.IOException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.OPEN_CONFIRM4resok;
import org.dcache.nfs.v4.xdr.OPEN_CONFIRM4res;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOPEN_CONFIRM extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationOPEN_CONFIRM.class);

    OperationOPEN_CONFIRM(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN_CONFIRM);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final OPEN_CONFIRM4res res = result.opopen_confirm;

        Inode inode = context.currentInode();
        Stat stat = context.getFs().getattr(context.currentInode());

        if (stat.type() == Stat.Type.DIRECTORY) {
            throw new ChimeraNFSException(nfsstat.NFSERR_ISDIR, "path is a directory");
        }

        if (stat.type() == Stat.Type.SYMLINK) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "path is a symlink");
        }

        stateid4 stateid = _args.opopen_confirm.open_stateid;
        _log.debug("confirmed stateID: {}", stateid);

        NFS4Client client = context.getStateHandler().getClientIdByStateId(stateid);
        if (client == null) {
            throw new ChimeraNFSException(nfsstat.NFSERR_BAD_STATEID, "bad client id.");
        }

        NFS4State state = client.state(stateid);
        if (state.stateid().seqid.value != _args.opopen_confirm.seqid.value.value) {
            throw new ChimeraNFSException(nfsstat.NFSERR_BAD_SEQID, "bad seqid.");
        }

        state.bumpSeqid();
        state.confirm();

        res.resok4 = new OPEN_CONFIRM4resok();
        res.resok4.open_stateid = state.stateid();

        res.status = nfsstat.NFS_OK;
    }
}
