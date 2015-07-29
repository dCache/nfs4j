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
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.SECINFO4resok;
import org.dcache.nfs.v4.xdr.SECINFO4res;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationSECINFO extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationSECINFO.class);

    public OperationSECINFO(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_SECINFO);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        final SECINFO4res res = result.opsecinfo;
        Inode dir = context.currentInode();
        Stat stat = context.getFs().getattr(dir);
        if (stat.type() != Stat.Type.DIRECTORY) {
            throw new NotDirException();
        }

        context.clearCurrentInode();

        String name = NameFilter.convertName(_args.opsecinfo.name.value);
        Inode inode = context.getFs().lookup(dir, name);

        try {
            res.resok4 = new SECINFO4resok();
            res.resok4.value = OperationSECINFO_NO_NAME.secinfosOf(inode, context);
            res.status = nfsstat.NFS_OK;
        } catch (GSSException e) {
            throw new NfsIoException(e.getMessage());
        }
    }
}
