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
import org.dcache.nfs.v4.xdr.utf8str_cs;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.fattr4_acl;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.settime4;
import org.dcache.nfs.v4.xdr.fattr4;
import org.dcache.nfs.v4.xdr.time_how4;
import org.dcache.nfs.v4.xdr.nfstime4;
import org.dcache.nfs.v4.xdr.uint64_t;
import org.dcache.nfs.v4.xdr.mode4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.SETATTR4res;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.AttrNotSuppException;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.OpenModeException;
import org.dcache.nfs.v4.acl.Acls;

import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcache.nfs.v4.NFSv4FileAttributes.SUPPORTED_ATTR_MASK_V4_1;

public class OperationSETATTR extends AbstractNFSv4Operation {


    private static final Logger _log = LoggerFactory.getLogger(OperationSETATTR.class);
    private static final bitmap4 EMPTY_BITMASK = new bitmap4();

    public OperationSETATTR(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_SETATTR);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        final SETATTR4res res = result.opsetattr;

        /*
         * on either success or failure of the operation, the server will return
         * the attrsset bitmask to represent what (if any) attributes were
         * successfully set.
         *
         * Initialize with an empty bitmask, which will be replaces on success.
         */
        res.attrsset = EMPTY_BITMASK;

        Inode inode = context.currentInode();

        if (_args.opsetattr.obj_attributes.attrmask.isSet(nfs4_prot.FATTR4_SIZE) && !Stateids.isStateLess(_args.opsetattr.stateid)) {

            // TODO: check for DENY_WRITE for any existing opens. However, posix does not support deny masks.
            NFS4Client client;
            stateid4 stateid = Stateids.getCurrentStateidIfNeeded(context, _args.opsetattr.stateid);
            if (context.getMinorversion() > 0) {
                client = context.getSession().getClient();
            } else {
                client = context.getStateHandler().getClientIdByStateId(stateid);
            }

            // will throw BAD_STATEID
            NFS4State state = client.state(stateid);

            // setting file size requires open for writing
            int shareAccess = context.getStateHandler().getFileTracker().getShareAccess(client, inode,
                    state.getOpenState().stateid());
            if ((shareAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) == 0) {
                throw new OpenModeException("Invalid open mode");
            }
        }

        res.status = nfsstat.NFS_OK;
        res.attrsset = setAttributes(_args.opsetattr.obj_attributes, inode, context);
    }

    static bitmap4 setAttributes(fattr4 attributes, Inode inode, CompoundContext context) throws IOException, OncRpcException {

        XdrBuffer xdr = new XdrBuffer(attributes.attr_vals.value);
        xdr.beginDecoding();

        /*
         * bitmap we send back. can't be uninitialized.
         */
        bitmap4 processedAttributes = new bitmap4();
        Stat stat = new Stat();
        try {
            for (int i : attributes.attrmask) {
                xdr2fattr(i, stat, inode, context, xdr);
                _log.debug("   setAttributes : {} ({}) OK", i, OperationGETATTR.attrMask2String(i));
                processedAttributes.set(i);
            }
        }catch (BadXdrOncRpcException e) {
            throw new BadXdrException(e.getMessage());
        }

        if (xdr.hasMoreData()) {
            throw new BadXdrException("garbage in attr bitmap");
        }
        xdr.endDecoding();

        context.getFs().setattr(inode, stat);
        return processedAttributes;
    }

    static void xdr2fattr(int fattr , Stat stat, Inode inode, CompoundContext context, XdrDecodingStream xdr) throws IOException, OncRpcException {

        switch(fattr) {

            case nfs4_prot.FATTR4_SIZE :
                uint64_t size = new uint64_t();
                size.xdrDecode(xdr);
                stat.setSize(size.value);
                break;
            case nfs4_prot.FATTR4_ACL :
                fattr4_acl acl = new fattr4_acl();
                acl.xdrDecode(xdr);

                context.getFs().setAcl(inode, acl.value);
                stat.setMTime(System.currentTimeMillis());
                break;
            case nfs4_prot.FATTR4_MODE :
                mode4 mode = new mode4();
                mode.xdrDecode(xdr);
                stat.setMode(mode.value);
                context.getFs().setAcl(inode, Acls.adjust( context.getFs().getAcl(inode), mode.value));
                break;
            case nfs4_prot.FATTR4_OWNER :
                utf8str_cs owner = new utf8str_cs ();
                owner.xdrDecode(xdr);
                String new_owner = owner.toString();
                if (new_owner.isEmpty()) {
                    throw new InvalException("empty principal");
                }
                stat.setUid(context.getFs().getIdMapper().principalToUid(new_owner));
                break;
            case nfs4_prot.FATTR4_OWNER_GROUP :
                utf8str_cs owner_group = new utf8str_cs ();
                owner_group.xdrDecode(xdr);
                String new_group = owner_group.toString();
                if (new_group.isEmpty()) {
                    throw new InvalException("empty principal");
                }
                stat.setGid(context.getFs().getIdMapper().principalToGid(new_group));
                break;
            case nfs4_prot.FATTR4_TIME_ACCESS_SET :
                settime4 atime = new settime4();
                atime.xdrDecode(xdr);
                // ignore for performance

                break;
            case nfs4_prot.FATTR4_TIME_CREATE :
                nfstime4 ctime = new nfstime4();
                ctime.xdrDecode(xdr);
                stat.setCTime(ctime.toMillis());
                break;
            case nfs4_prot.FATTR4_TIME_MODIFY_SET :
                settime4 setMtime = new settime4();
                setMtime.xdrDecode(xdr);

                long realMtime = setMtime.set_it == time_how4.SET_TO_SERVER_TIME4 ?
                    System.currentTimeMillis() : setMtime.time.toMillis();

                stat.setMTime(realMtime);
                break;
            default:
                if (SUPPORTED_ATTR_MASK_V4_1.isSet(fattr)) {
                    throw new InvalException("Read-only attribute: " + fattr);
                } else {
                    throw new AttrNotSuppException("Attribute not supported: " + fattr);
                }
        }
    }

}
