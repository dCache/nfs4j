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
import org.dcache.nfs.status.LockRangeException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.nlm.LockException;
import org.dcache.nfs.v4.nlm.LockRangeUnavailabeException;
import org.dcache.nfs.v4.nlm.NlmLock;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLOCKU extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationLOCKU.class);

    public OperationLOCKU(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_LOCKU);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {
        // to enforce current file handle existence check
        Inode inode = context.currentInode();

        stateid4 lockStateid = Stateids.getCurrentStateidIfNeeded(context, _args.oplocku.lock_stateid);
        NFS4Client client;
        StateOwner lockOwner;
        NFS4State lock_state;
        try {

            if (context.getMinorversion() == 0) {
                client = context.getStateHandler().getClientIdByStateId(lockStateid);
            } else {
                client = context.getSession().getClient();
            }

            lock_state = client.state(lockStateid);
            lockOwner = lock_state.getStateOwner();
            if (context.getMinorversion() == 0) {
                lockOwner.acceptAsNextSequence(_args.oplocku.seqid);
            }

            NlmLock lock = new NlmLock(lockOwner, _args.oplocku.locktype, _args.oplocku.offset.value, _args.oplocku.length.value);
            try {
                context.getLm().unlock(inode.getFileId(), lock);
            } catch (LockRangeUnavailabeException e) {
                // posix locks allows unlocking of not locked regions
            }

            lock_state.bumpSeqid();
            context.currentStateid(lock_state.stateid());
            result.oplocku.status = nfsstat.NFS_OK;
            result.oplocku.lock_stateid = lock_state.stateid();

        } catch (LockRangeUnavailabeException e) {
            throw new LockRangeException();
        } catch (LockException e) {
            throw new ServerFaultException("lock error", e);
        }

    }
}
