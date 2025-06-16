/*
 * Copyright (c) 2019 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;

/**
 *
 */
public class OperationLAYOUTSTATS extends AbstractNFSv4Operation {

    public OperationLAYOUTSTATS(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_LAYOUTSTATS);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final NFSv41DeviceManager pnfsDeviceManager = context
                .getDeviceManager()
                .orElseThrow(() -> new NotSuppException("pNFS device manager not configured"));

        pnfsDeviceManager.layoutStats(context, _args.oplayoutstats);

        result.oplayoutstats.status = nfsstat.NFS_OK;
    }

}
