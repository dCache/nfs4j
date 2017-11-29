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
package org.dcache.nfs.v4.ds;

import java.io.IOException;
import java.nio.channels.FileChannel;

import org.dcache.nfs.v4.AbstractNFSv4Operation;
import org.dcache.nfs.v4.CompoundContext;
import org.dcache.nfs.v4.xdr.WRITE4res;
import org.dcache.nfs.v4.xdr.WRITE4resok;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.v4.Stateids;
import org.dcache.nfs.v4.xdr.stable_how4;
import org.dcache.nfs.vfs.FsCache;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSOperationWRITE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(DSOperationWRITE.class);
    private final FsCache _fsCache;

    public DSOperationWRITE(nfs_argop4 args, FsCache fsCache) {
        super(args, nfs_opnum4.OP_WRITE);
        _fsCache = fsCache;
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final WRITE4res res = result.opwrite;

        long offset = _args.opwrite.offset.value;

        _args.opwrite.offset.checkOverflow(_args.opwrite.data.remaining(), "offset + length overflow");

        Inode inode = context.currentInode();
        Stat stat = context.getFs().getattr(inode);

        if (stat.type() == Stat.Type.DIRECTORY) {
            throw new IsDirException("Can't WRITE into a directory inode");
        }

        if (stat.type() != Stat.Type.REGULAR) {
            throw new InvalException("Invalid object type");
        }

        if ((context.getMinorversion() == 0) && !Stateids.ZeroStateId().equalsWithSeq(_args.opwrite.stateid) && !Stateids.OneStateId().equalsWithSeq(_args.opwrite.stateid)) {
            /*
             *  The NFSv4.0 spec requires to update lease time as long as client
             * needs the file. This is done through READ, WRITE and RENEW
             * opertations. With introduction of sessions in v4.1 update of the
             * lease time done through SEQUENCE operation.
             */
            context.getStateHandler().updateClientLeaseTime(_args.opwrite.stateid);
        }

        FileChannel out = _fsCache.get(inode);

        long lastSize = out.size();

        _args.opwrite.data.rewind();
        int bytesWritten = out.write(_args.opwrite.data, offset);

        if (bytesWritten < 0) {
            throw new NfsIoException("IO not allowd");
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new WRITE4resok();
        res.resok4.count = new count4(bytesWritten);
        res.resok4.committed = _args.opwrite.stable;
        res.resok4.writeverf = context.getRebootVerifier();

        synchronized(out) {
            if ((_args.opwrite.stable != stable_how4.UNSTABLE4) && (offset + bytesWritten > lastSize)) {
                Stat newStat = new Stat();
                newStat.setSize(out.size());
                context.getFs().setattr(context.currentInode(), newStat);
            }
        }
        _log.debug("MOVER: {}@{} written, {} requested. New File size {}",
                bytesWritten, offset, _args.opwrite.data, out.size());
    }
}
