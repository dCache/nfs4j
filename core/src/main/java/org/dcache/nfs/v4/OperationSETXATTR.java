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
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.v4.xdr.change_info4;
import org.dcache.nfs.v4.xdr.changeid4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.oncrpc4j.rpc.OncRpcException;

public class OperationSETXATTR extends AbstractNFSv4Operation {

    public OperationSETXATTR(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_SETXATTR);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        Inode inode = context.currentInode();

        result.opsetxattr.sxr_info = new change_info4();
        result.opsetxattr.sxr_info.atomic = true;
        Stat stat = context.getFs().getattr(inode);
        result.opsetxattr.sxr_info.before = new changeid4(stat.getGeneration());

        context.getFs().setXattr(inode, _args.opsetxattr.sxa_name,
                _args.opsetxattr.sxa_value.value,
                toXatterSetMode(_args.opsetxattr.sxa_option));

        stat = context.getFs().getattr(inode);
        result.opsetxattr.sxr_info.after = new changeid4(stat.getGeneration());

        result.setStatus(nfsstat.NFS_OK);
    }

    private final VirtualFileSystem.SetXattrMode toXatterSetMode(int i) throws BadXdrException {
        switch(i) {
            case 0:
                return VirtualFileSystem.SetXattrMode.EITHER;
            case 1:
                return VirtualFileSystem.SetXattrMode.CREATE;
            case 2:
                return VirtualFileSystem.SetXattrMode.REPLACE;
            default:
                throw new BadXdrException("Unknown setxattr mode " + i);
        }
    }

}
