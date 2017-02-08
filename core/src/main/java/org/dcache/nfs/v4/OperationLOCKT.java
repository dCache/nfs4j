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
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.nlm.LockDeniedException;
import org.dcache.nfs.v4.nlm.LockException;
import org.dcache.nfs.v4.nlm.NlmLock;
import org.dcache.nfs.v4.xdr.LOCK4denied;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.lock_owner4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLOCKT extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationLOCKT.class);

    public OperationLOCKT(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_LOCKT);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {
        // to enforce current file handle existence check
        Inode inode = context.currentInode();

        if (_args.oplockt.length.value == 0) {
            throw new InvalException("zero lock len");
        }

        try {

            /*
            * this is a hypothetic lock owner, which used for lock-test and might
            * not exist on the server.
            */
            state_owner4 hypotheticLockOwner = new state_owner4();
            hypotheticLockOwner.clientid = _args.oplockt.owner.clientid;
            hypotheticLockOwner.owner = _args.oplockt.owner.owner;

            StateOwner lockOwner = new StateOwner(hypotheticLockOwner, 0);

            NlmLock lock = new NlmLock(lockOwner, _args.oplockt.locktype, _args.oplockt.offset.value, _args.oplockt.length.value);
            context.getLm().test(inode.getFileId(), lock);

            result.oplockt.status = nfsstat.NFS_OK;

        } catch (LockDeniedException e) {
            result.oplockt.status = nfsstat.NFSERR_DENIED;
            NlmLock conflictingLock = e.getConflictingLock();
            result.oplockt.denied = new LOCK4denied();
            result.oplockt.denied.offset = new offset4(conflictingLock.getOffset());
            result.oplockt.denied.length = new length4(conflictingLock.getLength());
            result.oplockt.denied.locktype = conflictingLock.getLockType();
            result.oplockt.denied.owner = new lock_owner4(conflictingLock.getOwner().getRawStateOwner());
        } catch (LockException e) {
            throw new ServerFaultException("lock error", e);
        }

    }
}
