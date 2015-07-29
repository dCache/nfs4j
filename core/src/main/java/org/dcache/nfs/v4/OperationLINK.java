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
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.change_info4;
import org.dcache.nfs.v4.xdr.changeid4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.LINK4resok;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLINK extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationLINK.class);

    public OperationLINK(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_LINK);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        String newName = NameFilter.convertName(_args.oplink.newname.value);

        Stat parentDirStat = context.getFs().getattr(context.currentInode());
        Stat inodeStat = context.getFs().getattr(context.savedInode());

        if (parentDirStat.type() != Stat.Type.DIRECTORY) {
            throw new NotDirException("Can't create a hard-link in non directory object");
        }

        if (inodeStat.type() == Stat.Type.DIRECTORY) {
            throw new IsDirException("Can't hard-link a directory");
        }

        context.getFs().link(context.currentInode(), context.savedInode(), newName,
                context.getSubject());

        result.oplink.resok4 = new LINK4resok();
        result.oplink.resok4.cinfo = new change_info4();
        result.oplink.resok4.cinfo.atomic = true;
        result.oplink.resok4.cinfo.before = new changeid4(parentDirStat.getCTime());
        result.oplink.resok4.cinfo.after = new changeid4(System.currentTimeMillis());

        result.oplink.status = nfsstat.NFS_OK;
    }
}
