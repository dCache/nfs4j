/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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
import java.nio.ByteBuffer;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.READ4resok;
import org.dcache.nfs.v4.xdr.READ4res;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationREAD extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationREAD.class);

    public OperationREAD(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_READ);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {
        final READ4res res = result.opread;

        Stat inodeStat = context.getFs().getattr(context.currentInode());

        if (inodeStat.type() == Stat.Type.DIRECTORY) {
            throw new IsDirException();
        }

        if (inodeStat.type() == Stat.Type.SYMLINK) {
            throw new InvalException();
        }

        if (context.getMinorversion() == 0) {
            /*
             *  The NFSv4.0 spec requires to update lease time as long as client
             * needs the file. This is done through READ, WRITE and RENEW
             * opertations. With introduction of sessions in v4.1 update of the
             * lease time done through SEQUENCE operation.
             */
            context.getStateHandler().updateClientLeaseTime(_args.opread.stateid);
        }


        long offset = _args.opread.offset.value;
        int count = _args.opread.count.value;

        ByteBuffer buf = ByteBuffer.allocate(count);

        int bytesReaded = context.getFs().read(context.currentInode(),
                buf.array(), offset, count);
        if (bytesReaded < 0) {
            throw new NfsIoException("IO not allowd");
        }

        /*
         * As we have written directly into back-end byte array tell the byte
         * buffer where the limit is.
         */
        buf.limit(bytesReaded);

        res.status = nfsstat.NFS_OK;
        res.resok4 = new READ4resok();

        res.resok4.data = buf;

        if (offset + bytesReaded >= inodeStat.getSize()) {
            res.resok4.eof = true;
        }
    }
}
