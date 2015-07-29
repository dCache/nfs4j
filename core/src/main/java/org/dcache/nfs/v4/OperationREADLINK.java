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

import java.io.IOException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.linktext4;
import org.dcache.nfs.v4.xdr.utf8str_cs;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.READLINK4res;
import org.dcache.nfs.v4.xdr.READLINK4resok;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationREADLINK extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationREADLINK.class);

    public OperationREADLINK(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_READLINK);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {
        final READLINK4res res = result.opreadlink;

        Stat stat = context.getFs().getattr(context.currentInode());
        if (stat.type() != Stat.Type.SYMLINK) {
            throw new InvalException("not a symlink");
        }

        String link = context.getFs().readlink(context.currentInode());
        _log.debug("NFS Request  READLINK4 link: {}", link);
        res.resok4 = new READLINK4resok();
        res.resok4.link = new linktext4();
        res.resok4.link.value = new utf8str_cs(link);
        res.status = nfsstat.NFS_OK;
    }
}
