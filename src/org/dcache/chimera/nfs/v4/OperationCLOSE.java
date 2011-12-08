/*
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

package org.dcache.chimera.nfs.v4;

import java.io.IOException;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.CLOSE4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationCLOSE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationCLOSE.class);

    OperationCLOSE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_CLOSE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {
        final CLOSE4res res = result.opclose;

        Inode inode = context.currentInode();

        if (context.getMinorversion() > 0) {
            context.getSession().getClient().updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);

            /*
             * for now it's just a place holder for state processing.
             * Nevertheless, NFSERR_BAD_STATEID is throws if state is invalid.
             */
            NFS4State state = context.getSession().getClient().state(_args.opclose.open_stateid);
            try {
                context.getDeviceManager().layoutReturn(context, _args.opclose.open_stateid);
            } catch (IOException e) {
                _log.error("Failed to return a layout: {}", e.getMessage());
            }
        } else {
            context.getStateHandler().updateClientLeaseTime(_args.opclose.open_stateid);
        }

        res.open_stateid = Stateids.invalidStateId();
        res.status = nfsstat.NFS_OK;

    }
}
