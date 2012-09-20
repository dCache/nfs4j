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
import org.dcache.chimera.nfs.v4.xdr.open_delegation_type4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.opentype4;
import org.dcache.chimera.nfs.v4.xdr.open_claim_type4;
import org.dcache.chimera.nfs.v4.xdr.open_delegation4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.createmode4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPEN4resok;
import org.dcache.chimera.nfs.v4.xdr.OPEN4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.FileExistsChimeraFsException;
import org.dcache.chimera.FileNotFoundHimeraFsException;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.vfs.Inode;
import org.dcache.chimera.nfs.vfs.Stat;
import org.dcache.xdr.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOPEN extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationOPEN.class);

    OperationOPEN(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {
        final OPEN4res res = result.opopen;

        try {
            Long clientid = Long.valueOf(_args.opopen.owner.value.clientid.value.value);
            NFS4Client client;

            if (context.getMinorversion() > 0) {
                client = context.getSession().getClient();
            } else {
                client = context.getStateHandler().getClientByID(clientid);

                if (client == null || !client.isConfirmed()) {
                    throw new ChimeraNFSException(nfsstat.NFSERR_STALE_CLIENTID, "bad client id.");
                }

                client.updateLeaseTime();
                _log.debug("open request form clientid: {}, owner: {}",
                        client, new String(_args.opopen.owner.value.owner));
            } 

            res.resok4 = new OPEN4resok();
            res.resok4.attrset = new bitmap4();
            res.resok4.attrset.value = new uint32_t[2];
            res.resok4.attrset.value[0] = new uint32_t(0);
            res.resok4.attrset.value[1] = new uint32_t(0);
            res.resok4.delegation = new open_delegation4();
            res.resok4.delegation.delegation_type = open_delegation_type4.OPEN_DELEGATE_NONE;

            switch (_args.opopen.claim.claim) {

                case open_claim_type4.CLAIM_NULL:

                    Stat stat = context.getFs().getattr(context.currentInode());
                    if (stat.type() != Stat.Type.DIRECTORY) {
                        throw new ChimeraNFSException(nfsstat.NFSERR_NOTDIR, "not a directory");
                    }

                    String name = NameFilter.convert(_args.opopen.claim.file.value.value.value);
                    _log.debug("regular open for : {}", name);

                    Inode inode;
                    if (_args.opopen.openhow.opentype == opentype4.OPEN4_CREATE) {

                        boolean exclusive = (_args.opopen.openhow.how.mode == createmode4.EXCLUSIVE4) ||
                                (_args.opopen.openhow.how.mode == createmode4.EXCLUSIVE4_1);

                        try {

                            inode = context.getFs().lookup(context.currentInode(), name);

                            if (exclusive) {
                                throw new ChimeraNFSException(nfsstat.NFSERR_EXIST, "file already exist");
                            }

                            Stat fileStat = context.getFs().getattr(context.currentInode());
                            _log.debug("Opening existing file: {}, uid: {}, gid: {}, mode: 0{}",
                                    new Object[] {
                                        name,
                                        fileStat.getUid(),
                                        fileStat.getGid(),
                                        Integer.toOctalString(fileStat.getMode() & 0777)
                                    } );

                            if (context.getFs().access(inode, nfs4_prot.ACCESS4_MODIFY) == 0) {
                                throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "Permission denied.");
                            }

                            OperationSETATTR.setAttributes(_args.opopen.openhow.how.createattrs, inode, context);
                        } catch (FileNotFoundHimeraFsException he) {

                            _log.debug("Creating a new file: {}", name);
                            inode = context.getFs().create(context.currentInode(), Stat.Type.REGULAR,
                                    name, context.getUser().getUID(),
                                    context.getUser().getGID(), 0600);

                            // FIXME: proper implementation required
                            switch (_args.opopen.openhow.how.mode) {
                                case createmode4.UNCHECKED4:
                                case createmode4.GUARDED4:
                                    res.resok4.attrset = OperationSETATTR.setAttributes(_args.opopen.openhow.how.createattrs, inode, context);
                                    break;
                                case createmode4.EXCLUSIVE4:
                                case createmode4.EXCLUSIVE4_1:
                            }
                        }

                    } else {

                        inode = context.getFs().lookup(context.currentInode(), name);
                        stat = context.getFs().getattr(inode);

                        if ( context.getFs().access(inode, nfs4_prot.ACCESS4_READ) == 0) {
                            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "Permission denied.");
                        }

                        if (stat.type() == Stat.Type.DIRECTORY) {
                            throw new ChimeraNFSException(nfsstat.NFSERR_ISDIR, "path is a directory");
                        }

                        if (stat.type() == Stat.Type.SYMLINK) {
                            throw new ChimeraNFSException(nfsstat.NFSERR_SYMLINK, "path is a symlink");
                        }
                    }

                    context.currentInode(inode);

                    break;
                case open_claim_type4.CLAIM_PREVIOUS:
                    _log.debug("open by Inode for : {}", context.currentInode());
                    break;
                case open_claim_type4.CLAIM_DELEGATE_CUR:
                    break;
                case open_claim_type4.CLAIM_DELEGATE_PREV:
                    break;
            }

            res.resok4.cinfo = new change_info4();
            res.resok4.cinfo.atomic = true;
            res.resok4.cinfo.before = new changeid4(new uint64_t(context.getFs().getattr(context.currentInode()).getMTime()));
            res.resok4.cinfo.after = new changeid4(new uint64_t(System.currentTimeMillis()));

            /*
             * if it's v4.0, then client have to confirm
             */
            if (context.getMinorversion() > 0) {
                res.resok4.rflags = new uint32_t(nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX);
            }else {
                res.resok4.rflags = new uint32_t(nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX
                        | nfs4_prot.OPEN4_RESULT_CONFIRM);                
            }

            NFS4State nfs4state = client.createState();
            res.resok4.stateid = nfs4state.stateid();

            _log.debug("New stateID: {}", nfs4state.stateid());

            res.status = nfsstat.NFS_OK;

        } catch (FileExistsChimeraFsException e) {
            _log.debug("OPEN: {}", e.getMessage());
            res.status = nfsstat.NFSERR_EXIST;
        } catch (FileNotFoundHimeraFsException fnf) {
            _log.debug("OPEN: {}", fnf.getMessage());
            res.status = nfsstat.NFSERR_NOENT;
        }
    }
}
