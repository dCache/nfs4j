/*
 * Copyright (c) 2015 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.v4.xdr.FREE_STATEID4res;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.oncrpc4j.rpc.OncRpcException;

/**
 * NFS operation FREE_STATEID, rfc5661#18.38
 */
public class OperationFREE_STATEID extends AbstractNFSv4Operation {

    public OperationFREE_STATEID(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_FREE_STATEID);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        // FIXME: we as validating spec, as a valid open-state must rerun NFS4ERR_LOCKS_HELD.

        final FREE_STATEID4res res = result.opfree_stateid;
        NFS4Client client = context.getSession().getClient();
        stateid4 stateid = Stateids.getCurrentStateidIfNeeded(context, _args.opfree_stateid.fsa_stateid);
        client.releaseState(stateid);
        res.fsr_status = nfsstat.NFS_OK;
    }
}
