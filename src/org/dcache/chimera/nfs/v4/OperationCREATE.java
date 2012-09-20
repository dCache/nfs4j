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
import org.dcache.chimera.nfs.v4.xdr.nfs_ftype4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.CREATE4res;
import org.dcache.chimera.nfs.v4.xdr.CREATE4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.vfs.Inode;
import org.dcache.xdr.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationCREATE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationCREATE.class);

    OperationCREATE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_CREATE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        final CREATE4res res = result.opcreate;

        fattr4 objAttr = _args.opcreate.createattrs;
        int type = _args.opcreate.objtype.type;
        Inode inode = null;

        String name = NameFilter.convert(_args.opcreate.objname.value.value.value);

        if (name.length() == 0) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "bad path name");
        }

        if (name.length() > NFSv4Defaults.NFS4_MAXFILENAME) {
            throw new ChimeraNFSException(nfsstat.NFSERR_NAMETOOLONG, "name too long");
        }

        if (context.currentInode().type() != Inode.Type.DIRECTORY) {
            throw new ChimeraNFSException(nfsstat.NFSERR_NOTDIR, "not a directory");
        }


        if (name.equals(".") || name.equals("..")) {
            throw new ChimeraNFSException(nfsstat.NFSERR_BADNAME, "bad name '.' or '..'");
        }

        // TODO: this check have to be moved into JdbcFs
        try {
            inode = context.getFs().lookup(context.currentInode(), name);
            throw new ChimeraNFSException(nfsstat.NFSERR_EXIST, "path already exist");
        } catch (ChimeraFsException hfe) {
        }

        switch (type) {

            case nfs_ftype4.NF4DIR:
                inode = context.getFs().mkdir(context.currentInode(), name,
                        context.getUser().getUID(), context.getUser().getGID(), 777);
                break;
            case nfs_ftype4.NF4LNK:
                String linkDest = new String(_args.opcreate.objtype.linkdata.value.value.value);
                inode = context.getFs().symlink(context.currentInode(), name, linkDest,
                        context.getUser().getUID(), context.getUser().getGID(), 777);
                break;
            case nfs_ftype4.NF4BLK:
                inode = context.getFs().create(context.currentInode(), Inode.Type.BLOCK, name,
                        context.getUser().getUID(), context.getUser().getGID(), 777);
                break;
            case nfs_ftype4.NF4CHR:
                inode = context.getFs().create(context.currentInode(), Inode.Type.CHAR, name,
                        context.getUser().getUID(), context.getUser().getGID(), 777);
                break;
            case nfs_ftype4.NF4FIFO:
                inode = context.getFs().create(context.currentInode(), Inode.Type.FIFO, name,
                        context.getUser().getUID(), context.getUser().getGID(), 777);
                break;
            case nfs_ftype4.NF4SOCK:
                inode = context.getFs().create(context.currentInode(), Inode.Type.SOCK, name,
                        context.getUser().getUID(), context.getUser().getGID(), 777);
                break;
            case nfs_ftype4.NF4ATTRDIR:
            case nfs_ftype4.NF4NAMEDATTR:
                throw new ChimeraNFSException(nfsstat.NFSERR_NOTSUPP, "create of this type not supported");
            // regular files handled by OPEN
            case nfs_ftype4.NF4REG:
                throw new ChimeraNFSException(nfsstat.NFSERR_BADTYPE, "create of regular files handled by OPEN");
            default:
                throw new ChimeraNFSException(nfsstat.NFSERR_BADTYPE, "bad file type");
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new CREATE4resok();
        res.resok4.attrset = OperationSETATTR.setAttributes(objAttr, inode, context);
        res.resok4.cinfo = new change_info4();
        res.resok4.cinfo.atomic = true;
        res.resok4.cinfo.before = new changeid4(new uint64_t(context.getFs().getattr(context.currentInode()).getMTime()));
        res.resok4.cinfo.after = new changeid4(new uint64_t(System.currentTimeMillis()));

        context.currentInode(inode);
    }
}
