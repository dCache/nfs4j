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
import java.util.Optional;

import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.nfs_ftype4;
import org.dcache.nfs.v4.xdr.fattr4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.change_info4;
import org.dcache.nfs.v4.xdr.changeid4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.CREATE4res;
import org.dcache.nfs.v4.xdr.CREATE4resok;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadTypeException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.mode4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationCREATE extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationCREATE.class);

    public OperationCREATE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_CREATE);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        final CREATE4res res = result.opcreate;

        fattr4 objAttr = _args.opcreate.createattrs;
        int type = _args.opcreate.objtype.type;
        Inode inode;

        Stat stat = context.getFs().getattr(context.currentInode());
        String name = NameFilter.convertName(_args.opcreate.objname.value);

        if (stat.type() != Stat.Type.DIRECTORY) {
            throw new NotDirException();
        }

        AttributeMap attributeMap = new AttributeMap(objAttr);
        bitmap4 appliedAttribytes = bitmap4.of(0);
        int mode = 700;
        Optional<mode4> createMode = attributeMap.get(nfs4_prot.FATTR4_MODE);
        if (createMode.isPresent()) {
            mode = createMode.get().value;
            appliedAttribytes.set(nfs4_prot.FATTR4_MODE);
        }


        switch (type) {

            case nfs_ftype4.NF4DIR:
                inode = context.getFs().mkdir(context.currentInode(), name,
                        context.getSubject(), mode);
                break;
            case nfs_ftype4.NF4LNK:
                String linkDest = NameFilter.convertPath(_args.opcreate.objtype.linkdata.value.value);
                inode = context.getFs().symlink(context.currentInode(), name, linkDest,
                        context.getSubject(), mode);
                break;
            case nfs_ftype4.NF4BLK:
                inode = context.getFs().create(context.currentInode(), Stat.Type.BLOCK, name,
                        context.getSubject(), mode);
                break;
            case nfs_ftype4.NF4CHR:
                inode = context.getFs().create(context.currentInode(), Stat.Type.CHAR, name,
                        context.getSubject(), mode);
                break;
            case nfs_ftype4.NF4FIFO:
                inode = context.getFs().create(context.currentInode(), Stat.Type.FIFO, name,
                        context.getSubject(), mode);
                break;
            case nfs_ftype4.NF4SOCK:
                inode = context.getFs().create(context.currentInode(), Stat.Type.SOCK, name,
                        context.getSubject(), mode);
                break;
            case nfs_ftype4.NF4ATTRDIR:
            case nfs_ftype4.NF4NAMEDATTR:
                throw new NotSuppException("create of this type not supported");
            // regular files handled by OPEN
            case nfs_ftype4.NF4REG:
                throw new BadTypeException("create of regular files handled by OPEN");
            default:
                throw new BadTypeException("bad file type: " + type);
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new CREATE4resok();
        res.resok4.attrset = appliedAttribytes;
        res.resok4.cinfo = new change_info4();
        res.resok4.cinfo.atomic = true;
        res.resok4.cinfo.before = new changeid4(stat.getMTime());
        res.resok4.cinfo.after = new changeid4(System.currentTimeMillis());

        context.currentInode(inode);
        context.currentStateid(Stateids.ZeroStateId());
    }
}
