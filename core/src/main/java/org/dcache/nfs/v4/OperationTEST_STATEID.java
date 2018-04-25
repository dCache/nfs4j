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
import org.dcache.nfs.v4.xdr.TEST_STATEID4res;
import org.dcache.nfs.v4.xdr.TEST_STATEID4resok;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.oncrpc4j.rpc.OncRpcException;

/**
 * NFS operation TEST_STATEID, rfc5661#18.48
 */
public class OperationTEST_STATEID extends AbstractNFSv4Operation {

    public OperationTEST_STATEID(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_TEST_STATEID);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {
        final TEST_STATEID4res res = result.optest_stateid;
        res.tsr_resok4 = new TEST_STATEID4resok();
        res.tsr_resok4.tsr_status_codes = new int[_args.optest_stateid.ts_stateids.length];
        res.tsr_status = nfsstat.NFS_OK;

        NFS4Client client = context.getSession().getClient();
        for(int i = 0; i < _args.optest_stateid.ts_stateids.length; i++) {
            stateid4 statid = _args.optest_stateid.ts_stateids[i];
            try {
                NFS4State state = client.state(statid);
                if (state.stateid().seqid.value < statid.seqid.value) {
                    res.tsr_resok4.tsr_status_codes[i] = nfsstat.NFSERR_OLD_STATEID;
                } else {
                    res.tsr_resok4.tsr_status_codes[i] = nfsstat.NFS_OK;
                }
            } catch (ChimeraNFSException e) {
                res.tsr_resok4.tsr_status_codes[i] = e.getStatus();
            }
        }
    }

}
