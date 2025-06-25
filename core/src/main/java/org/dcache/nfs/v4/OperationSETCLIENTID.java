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

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.ClidInUseException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.v4.xdr.SETCLIENTID4res;
import org.dcache.nfs.v4.xdr.SETCLIENTID4resok;
import org.dcache.nfs.v4.xdr.clientaddr4;
import org.dcache.nfs.v4.xdr.netaddr4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationSETCLIENTID extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationSETCLIENTID.class);

    public OperationSETCLIENTID(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_SETCLIENTID);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        final SETCLIENTID4res res = result.opsetclientid;

        if (context.getMinorversion() > 0) {
            throw new NotSuppException("operation SETCLIENTID4 is obsolete in 4.x, x > 0");
        }

        verifier4 verifier = _args.opsetclientid.client.verifier;
        final byte[] id = _args.opsetclientid.client.id;
        NFS4Client client = context.getStateHandler().clientByOwner(id);


        if (client == null) {
            // new client
            client = context.getStateHandler().createClient(
                    context.getRemoteSocketAddress(),
                    context.getLocalSocketAddress(),
                    context.getMinorversion(),
                    _args.opsetclientid.client.id, _args.opsetclientid.client.verifier,
                    context.getPrincipal(), false);
        } else if (!client.isConfirmed()) {

            // existing client, but not confirmed. either retry or client restarted before confirmation
            context.getStateHandler().removeClient(client);
            client = context.getStateHandler().createClient(
                    context.getRemoteSocketAddress(),
                    context.getLocalSocketAddress(),
                    context.getMinorversion(),
                    _args.opsetclientid.client.id, _args.opsetclientid.client.verifier,
                    context.getPrincipal(), false);

        } else if (!client.clientGeneratedVerifierEquals(verifier)) {

            // existing client, different verifier. Client rebooted.
            // create new record, keep the old one as required by the RFC 7530
            client = context.getStateHandler().createClient(
                    context.getRemoteSocketAddress(),
                    context.getLocalSocketAddress(),
                    context.getMinorversion(),
                    _args.opsetclientid.client.id, _args.opsetclientid.client.verifier,
                    context.getPrincipal(), false);

        } else if (client.isLeaseValid()) {

            // can't be reused, if principal have changes and client has state
            if (!client.principal().equals(context.getPrincipal()) && client.hasState()) {
                netaddr4 addr = new netaddr4(client.getRemoteAddress());
                res.status = nfsstat.NFSERR_CLID_INUSE;
                res.client_using = new clientaddr4(addr);
                throw new ClidInUseException();
            }

            client.reset();
        }

        res.resok4 = new SETCLIENTID4resok();
        res.resok4.clientid = client.getId();
        res.resok4.setclientid_confirm = client.serverGeneratedVerifier();
        res.status = nfsstat.NFS_OK;
    }
}
