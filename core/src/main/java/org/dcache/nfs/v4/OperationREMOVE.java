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
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.change_info4;
import org.dcache.nfs.v4.xdr.changeid4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.REMOVE4resok;
import org.dcache.nfs.v4.xdr.REMOVE4res;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationREMOVE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationREMOVE.class);

    public OperationREMOVE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_REMOVE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        final REMOVE4res res = result.opremove;
        res.status = nfsstat.NFS_OK;
        res.resok4 = new REMOVE4resok();
        res.resok4.cinfo = new change_info4();
        res.resok4.cinfo.atomic = true;

        Inode parentInode = context.currentInode();
        Stat stat = context.getFs().getattr(parentInode);

        if (stat.type() != Stat.Type.DIRECTORY) {
            throw new NotDirException();
        }

        String name = NameFilter.convertName(_args.opremove.target.value);

        _log.debug("REMOVE: {} : {}", parentInode, name);

        res.resok4.cinfo.before = new changeid4(stat.getGeneration());
        context.getFs().remove(parentInode, name);

        res.resok4.cinfo.after = new changeid4(context.getFs().getattr(parentInode).getGeneration());
    }
}
