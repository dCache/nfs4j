/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.v4.xdr.open_delegation_type4;
import org.dcache.nfs.v4.xdr.change_info4;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.changeid4;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.opentype4;
import org.dcache.nfs.v4.xdr.open_claim_type4;
import org.dcache.nfs.v4.xdr.open_delegation4;
import org.dcache.nfs.v4.xdr.createmode4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.OPEN4resok;
import org.dcache.nfs.v4.xdr.OPEN4res;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.status.ExistException;
import org.dcache.nfs.status.GraceException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.IsDirException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.status.NoGraceException;
import org.dcache.nfs.status.SymlinkException;
import org.dcache.nfs.status.WrongTypeException;
import org.dcache.nfs.v4.xdr.fattr4_size;
import org.dcache.nfs.v4.xdr.mode4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOPEN extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationOPEN.class);

    public OperationOPEN(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {
        final OPEN4res res = result.opopen;

        NFS4Client client;
        StateOwner owner;
        if (context.getMinorversion() > 0) {
            client = context.getSession().getClient();
        } else {
            client = context.getStateHandler().getConfirmedClient(_args.opopen.owner.clientid);

            client.updateLeaseTime();
            _log.debug("open request form {}", _args.opopen.owner);
        }

        owner = client.getOrCreateOwner(_args.opopen.owner.owner, _args.opopen.seqid);

        res.resok4 = new OPEN4resok();
        res.resok4.attrset = new bitmap4();
        res.resok4.delegation = new open_delegation4();
        res.resok4.delegation.delegation_type = open_delegation_type4.OPEN_DELEGATE_NONE;
        res.resok4.cinfo = new change_info4();
        res.resok4.cinfo.atomic = true;

        switch (_args.opopen.claim.claim) {

            case open_claim_type4.CLAIM_NULL:

                if (client.needReclaim() && context.getStateHandler().isGracePeriod()) {
                    throw new GraceException();
                }

                Inode parent = context.currentInode();
                Stat stat = context.getFs().getattr(parent);
                if (stat.type() != Stat.Type.DIRECTORY) {
                    throw new NotDirException();
                }
                res.resok4.cinfo.before = new changeid4(stat.getGeneration());
                String name = NameFilter.convertName(_args.opopen.claim.file.value);
                _log.debug("regular open for : {}", name);

                Inode inode;
                if (_args.opopen.openhow.opentype == opentype4.OPEN4_CREATE) {

                    boolean exclusive = (_args.opopen.openhow.how.mode == createmode4.EXCLUSIVE4)
                            || (_args.opopen.openhow.how.mode == createmode4.EXCLUSIVE4_1);

                    /**
                     * According to the spec. client MAY send all allowed
                     * attributes. Nevertheless, in reality, clients send only
                     * mode. We will accept only mode and client will send extra
                     * SETATTR is required.
                     *
                     * REVISIT: we can apply all others as well to avoid extra
                     * network round trip.
                     */
                    AttributeMap attributeMap;

                    switch (_args.opopen.openhow.how.mode) {
                        case createmode4.UNCHECKED4:
                        case createmode4.GUARDED4:
                            attributeMap = new AttributeMap(_args.opopen.openhow.how.createattrs);
                            break;
                        case createmode4.EXCLUSIVE4:
                            attributeMap = new AttributeMap(null);
                            break;
                        case createmode4.EXCLUSIVE4_1:
                            attributeMap = new AttributeMap(_args.opopen.openhow.how.ch_createboth.cva_attrs);
                            break;
                        default:
                            throw new BadXdrException("bad value: " + _args.opopen.openhow.how.mode);
                    }

                    try {

                        int mode = 0600;

                        Optional<mode4> createMode = attributeMap.get(nfs4_prot.FATTR4_MODE);
                        Optional<fattr4_size> createSize = attributeMap.get(nfs4_prot.FATTR4_SIZE);

                        if (createMode.isPresent()) {
                            mode = createMode.get().value;
                        }

                        _log.debug("Creating a new file: {}", name);
                        inode = context.getFs().create(context.currentInode(), Stat.Type.REGULAR,
                                name, context.getSubject(), mode);

                        /*
                         * Tell client which attributes was applied.
                         */
                        if (createMode.isPresent()) {
                            res.resok4.attrset.set(nfs4_prot.FATTR4_MODE);
                        }

                        if (createSize.isPresent() && createSize.get().value == 0) {
                            res.resok4.attrset.set(nfs4_prot.FATTR4_SIZE);
                        }

                        res.resok4.cinfo.after = new changeid4(context.getFs().getattr(parent).getGeneration());
                    } catch (ExistException e) {

                        if (exclusive) {
                            throw new ExistException();
                        }
                        // no changes from us, old stat info is still good enough
                        res.resok4.cinfo.after = new changeid4(stat.getGeneration());

                        inode = context.getFs().lookup(context.currentInode(), name);
                        if (_log.isDebugEnabled()) {
                            Stat fileStat = context.getFs().getattr(context.currentInode());
                            _log.debug("Opening existing file: {}, uid: {}, gid: {}, mode: 0{}",
                                    name,
                                    fileStat.getUid(),
                                    fileStat.getGid(),
                                    Integer.toOctalString(fileStat.getMode() & 0777));
                        }

                        if (context.getFs().access(inode, nfs4_prot.ACCESS4_MODIFY) == 0) {
                            throw new AccessException();
                        }

                        Optional<fattr4_size> createSize = attributeMap.get(nfs4_prot.FATTR4_SIZE);
                        if (createSize.isPresent() && createSize.get().value == 0) {
                            Stat stat4size = new Stat();
                            stat4size.setSize(0);
                            context.getFs().setattr(inode, stat4size);
                            res.resok4.attrset.set(nfs4_prot.FATTR4_SIZE);
                        }

                    }

                } else {
                    // no changes from us, old stat info is still good enough
                    res.resok4.cinfo.after = new changeid4(stat.getGeneration());

                    inode = context.getFs().lookup(context.currentInode(), name);
                    checkCanAccess(context, inode, _args.opopen.share_access);
                }

                context.currentInode(inode);

                break;
            case open_claim_type4.CLAIM_PREVIOUS:
                /*
                 * As we don't have persistent state store there are two oprions:
                 *
                 *   1. fail with NFSERR_RECLAIM_BAD
                 *   2. just do a regulat open by FH.
                 *
                 * Let take the second case as first one will endup with
                 * it anyway.
                 *
                 * Just check that we are still in the grace period and
                 * fall -through to CLAIM_FH.
                 */
                if (!context.getStateHandler().isGracePeriod()) {
                    throw new NoGraceException("Server not in grace period");
                }
                if (!client.needReclaim()) {
                    throw new NoGraceException("CLAIM open after 'reclaim complete'");
                }
            case open_claim_type4.CLAIM_FH:

                _log.debug("open by Inode for : {}", context.currentInode());

                /*
                 * Send some dummy values for cinfo, as client
                 * does not really expect something. We can do a stat on parent,
                 * by this will be an extra fileststem (db) call which client
                 * will not use.
                 */
                res.resok4.cinfo.before = new changeid4(0);
                res.resok4.cinfo.after = new changeid4(0);

                inode = context.currentInode();
                checkCanAccess(context, inode, _args.opopen.share_access);
                break;
            case open_claim_type4.CLAIM_DELEGATE_CUR:
            case open_claim_type4.CLAIM_DELEGATE_PREV:
            case open_claim_type4.CLAIM_DELEG_CUR_FH:
            case open_claim_type4.CLAIM_DELEG_PREV_FH:
                _log.warn("Unimplemented open claim: {}", _args.opopen.claim.claim);
                throw new InvalException("Unimplemented open claim: {}" + _args.opopen.claim.claim);
            default:
                _log.warn("BAD open claim: {}", _args.opopen.claim.claim);
                throw new InvalException("BAD open claim: {}" + _args.opopen.claim.claim);

        }

        /*
         * if it's v4.0, then client have to confirm
         */
        if (context.getMinorversion() > 0) {
            res.resok4.rflags = new uint32_t(nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX);
        } else {
            res.resok4.rflags = new uint32_t(nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX
                    | nfs4_prot.OPEN4_RESULT_CONFIRM);
        }

        /*
         * NOTICE:
         * in case on concurrent non-exclusive created with share_deny == WRITE
         * may happen that client which have created the file will get DENY.
         *
         * THis is a perfectly a valid situation as at the end file is created and only
         * one writer is allowed.
         */
        stateid4 stateid = context
                .getStateHandler()
                .getFileTracker()
                .addOpen(client, owner, context.currentInode(),
                _args.opopen.share_access.value,
                _args.opopen.share_deny.value);

        context.currentStateid(stateid);
        res.resok4.stateid = stateid;
        res.status = nfsstat.NFS_OK;

    }


    private void checkCanAccess(CompoundContext context, Inode inode, uint32_t share_access) throws IOException {

        int accessMode;

        switch (share_access.value & ~nfs4_prot.OPEN4_SHARE_ACCESS_WANT_DELEG_MASK) {
            case nfs4_prot.OPEN4_SHARE_ACCESS_READ:
                accessMode = nfs4_prot.ACCESS4_READ;
                break;
            case nfs4_prot.OPEN4_SHARE_ACCESS_WRITE:
                accessMode = nfs4_prot.ACCESS4_MODIFY;
                break;
            case nfs4_prot.OPEN4_SHARE_ACCESS_BOTH:
                accessMode = nfs4_prot.ACCESS4_READ | nfs4_prot.ACCESS4_MODIFY;
                break;
            default:
                throw new InvalException("Invalid share_access mode: " + share_access.value);
        }

        if (context.getFs().access(inode, accessMode) != accessMode) {
            throw new AccessException();
        }

        Stat stat = context.getFs().getattr(inode);
        switch(stat.type()) {
            case REGULAR:
                // OK
                break;
            case DIRECTORY:
                throw new IsDirException();
            case SYMLINK:
                throw new SymlinkException();
            default:
                throw context.getMinorversion() == 0 ? new SymlinkException() : new WrongTypeException();
        }
    }
}
