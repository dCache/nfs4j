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
package org.dcache.nfs.v4.client;


import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_impl_id4;
import org.dcache.nfs.v4.xdr.utf8str_cis;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.state_protect4_a;
import org.dcache.nfs.v4.xdr.utf8str_cs;
import org.dcache.nfs.v4.xdr.EXCHANGE_ID4args;
import org.dcache.nfs.v4.xdr.nfstime4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.xdr.client_owner4;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.dcache.utils.Bytes;

public class ExchangeIDStub {

    private static final Random RANDOM = new Random(0);

    public static nfs_argop4 normal(String nii_domain, String nii_name,
            String co_ownerid, int flags, int how) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_EXCHANGE_ID;
        op.opexchange_id = new EXCHANGE_ID4args();
        op.opexchange_id.eia_client_impl_id = new nfs_impl_id4[1];
        nfs_impl_id4 n4 = new nfs_impl_id4();
        n4.nii_domain = new utf8str_cis(nii_domain);
        n4.nii_name = new utf8str_cs(nii_name);
        op.opexchange_id.eia_client_impl_id[0] = n4;

        nfstime4 releaseDate = new nfstime4();
        releaseDate.nseconds = 0;
        releaseDate.seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        op.opexchange_id.eia_client_impl_id[0].nii_date = releaseDate;
        op.opexchange_id.eia_clientowner = new client_owner4();
        op.opexchange_id.eia_clientowner.co_ownerid = co_ownerid.getBytes();

        op.opexchange_id.eia_clientowner.co_verifier = new verifier4();
        op.opexchange_id.eia_clientowner.co_verifier.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

        Bytes.putLong(op.opexchange_id.eia_clientowner.co_verifier.value, 0, RANDOM.nextLong());

        op.opexchange_id.eia_flags = new uint32_t(flags);
        op.opexchange_id.eia_state_protect = new state_protect4_a();
        op.opexchange_id.eia_state_protect.spa_how = how;
        return op;
    }

}
