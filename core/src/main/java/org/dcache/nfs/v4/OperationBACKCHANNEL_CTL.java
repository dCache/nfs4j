/*
 * Copyright (c) 2025 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class OperationBACKCHANNEL_CTL extends AbstractNFSv4Operation {

    public static final Logger LOG = LoggerFactory.getLogger(OperationBACKCHANNEL_CTL.class);


    public OperationBACKCHANNEL_CTL(nfs_argop4 op) {
        super(op, nfs_opnum4.OP_BACKCHANNEL_CTL);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        if( context.getMinorversion() == 0) {
            throw new NotSuppException("Backchannel update for minor version " + context.getMinorversion() + " not supported");
        }

        var session = context.getSession();
        var cb = new ClientCB(
                context.getRpcCall().getTransport().getPeerTransport(),
                _args.opbackchannel_ctl.bca_cb_program.value,
                context.getMinorversion(),
                session.id(),
                session.getMaxCbOps(),
                _args.opbackchannel_ctl.bca_sec_parms);

        try {
            cb.cbPing();
            session.getClient().setCB(cb);
        } catch (IOException | TimeoutException e) {
            LOG.warn("Failed to to replace backchannel for {} : {}", session.getClient(), e.getMessage());
            throw new NfsIoException(e.getMessage());
        }

        result.opbackchannel_ctl.bcr_status = nfsstat.NFS_OK;
    }
}
