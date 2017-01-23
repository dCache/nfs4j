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
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.lock_owner4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationRELEASE_LOCKOWNER extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationRELEASE_LOCKOWNER.class);

    public OperationRELEASE_LOCKOWNER(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_RELEASE_LOCKOWNER);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        lock_owner4 lockOwner = _args.oprelease_lockowner.lock_owner;

        if (context.getMinorversion() > 0) {
            throw new NotSuppException("operation RELEASE_LOCKOWNER is obsolete in 4.x, x > 0");
        }

        NFS4Client client = context.getStateHandler().getClientByID(lockOwner.clientid);
        client.releaseOwner(lockOwner.owner);

        result.oprelease_lockowner.status = nfsstat.NFS_OK;
    }
}
