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
import org.dcache.nfs.status.NoXattrException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.xattrvalue4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.oncrpc4j.rpc.OncRpcException;

public class OperationGETXATTR extends AbstractNFSv4Operation {

    public OperationGETXATTR(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_GETXATTR);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException,
            OncRpcException {

        try {
            Inode inode = context.currentInode();
            byte[] value = context.getFs().getXattr(inode, _args.opgetxattr.gxa_name);
            result.opgetxattr.gxr_value = new xattrvalue4(value);
            result.setStatus(nfsstat.NFS_OK);
        } catch (NotSuppException e) {
            throw e;
        } catch (IOException e) {
            throw new NoXattrException();
        }
    }
}
