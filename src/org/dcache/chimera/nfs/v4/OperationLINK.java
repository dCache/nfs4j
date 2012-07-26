/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.chimera.nfs.v4;

import java.io.IOException;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LINK4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLINK extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationLINK.class);

    OperationLINK(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_LINK);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        String newName = NameFilter.convert(_args.oplink.newname.value.value.value);

        Stat parentStat = context.currentInode().statCache();
        UnixAcl acl = new UnixAcl(parentStat.getUid(), parentStat.getGid(), parentStat.getMode() & 0777);
        if (!context.getAclHandler().isAllowed(acl, context.getUser(), AclHandler.ACL_INSERT)) {
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "Permission denied.");
        }

        context.getFs().link(context.currentInode(), context.savedInode(), newName,
                context.getUser().getUID(), context.getUser().getGID());

        result.oplink.resok4 = new LINK4resok();
        result.oplink.resok4.cinfo = new change_info4();
        result.oplink.resok4.cinfo.atomic = true;
        result.oplink.resok4.cinfo.before = new changeid4(new uint64_t(context.savedInode().statCache().getMTime()));
        result.oplink.resok4.cinfo.after = new changeid4(new uint64_t(System.currentTimeMillis()));

        result.oplink.status = nfsstat.NFS_OK;
    }
}
