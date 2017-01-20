/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.DESTROY_SESSION4res;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.ConnNotBoundToSessionException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationDESTROY_SESSION extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationDESTROY_SESSION.class);

    public OperationDESTROY_SESSION(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_DESTROY_SESSION);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        final DESTROY_SESSION4res res = result.opdestroy_session;

        NFS4Client client = context.getStateHandler().getClient(_args.opdestroy_session.dsa_sessionid);
        NFSv41Session session = client.getSession(_args.opdestroy_session.dsa_sessionid);

        SessionConnection sessionConnection = new SessionConnection(
                context.getLocalSocketAddress(),
                context.getRemoteSocketAddress());

        if (!session.isReleasableBy(sessionConnection)) {
            throw new ConnNotBoundToSessionException("Session not " +  session +" not bound to" + sessionConnection.getRemoteConnection());
        }

        client.removeSession(_args.opdestroy_session.dsa_sessionid);

        res.dsr_status = nfsstat.NFS_OK;
    }
}
