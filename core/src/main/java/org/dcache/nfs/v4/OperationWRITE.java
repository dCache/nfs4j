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

import java.io.IOException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.xdr.stable_how4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.WRITE4resok;
import org.dcache.nfs.v4.xdr.WRITE4res;
import org.dcache.nfs.ChimeraNFSException;

import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationWRITE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationWRITE.class);

    public OperationWRITE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_WRITE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        final WRITE4res res = result.opwrite;

        _args.opwrite.offset.checkOverflow(_args.opwrite.data.remaining(), "offset + length overflow");

        Stat stat = context.getFs().getattr(context.currentInode());

        if (stat.type() == Stat.Type.DIRECTORY) {
            throw new IsDirException();
        }

        if (stat.type() == Stat.Type.SYMLINK) {
            throw new InvalException("path is a symlink");
        }

        if (context.getMinorversion() > 0) {
            context.getSession().getClient().updateLeaseTime();
        } else {
            context.getStateHandler().updateClientLeaseTime(_args.opwrite.stateid);
        }

        long offset = _args.opwrite.offset.value;
        int count = _args.opwrite.data.remaining();
        byte[] data = new byte[count];
        _args.opwrite.data.get(data);

        VirtualFileSystem.WriteResult writeResult = context.getFs().write(context.currentInode(),
                data, offset, count, VirtualFileSystem.StabilityLevel.fromStableHow(_args.opwrite.stable));

        if (writeResult.getBytesWritten() < 0) {
            throw new NfsIoException("IO not allowed");
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new WRITE4resok();
        res.resok4.count = new count4(writeResult.getBytesWritten());
        res.resok4.committed = stable_how4.FILE_SYNC4;
        res.resok4.writeverf = new verifier4();
        res.resok4.writeverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

    }
}
