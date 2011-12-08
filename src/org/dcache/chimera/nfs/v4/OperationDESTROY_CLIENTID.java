/*
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program (see the file COPYING.LIB for more details); if not,
 * write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA
 * 02139, USA.
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.*;

public class OperationDESTROY_CLIENTID extends AbstractNFSv4Operation {

    public OperationDESTROY_CLIENTID(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_DESTROY_CLIENTID);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        final DESTROY_CLIENTID4res res = result.opdestroy_clientid;

        Long clientId = Long.valueOf(_args.opdestroy_clientid.dca_clientid.value.value);

        NFSv4StateHandler stateHandler = context.getStateHandler();
        NFS4Client client = stateHandler.getClientByID(clientId);
        stateHandler.removeClient(client);
        res.dcr_status = nfsstat.NFS_OK;
    }
}
