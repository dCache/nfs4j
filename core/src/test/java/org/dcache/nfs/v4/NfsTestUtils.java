/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.oncrpc4j.util.Bytes;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcAuthTypeUnix;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;

class NfsTestUtils {

    private final static Random RANDOM = new Random();
    private NfsTestUtils() { /* utility class */}

    static NFS4Client createClient() throws UnknownHostException {
        return createClient( new NFSv4StateHandler());
    }

    static NFS4Client createClient(NFSv4StateHandler stateHandler) throws UnknownHostException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(null), 123);
        byte[] owner = new byte[8];
        byte[] bootTime = new byte[8];
        RANDOM.nextBytes(owner);
        Bytes.putLong(bootTime, 0, System.currentTimeMillis());
        return stateHandler.createClient(address, address, 1, owner, new verifier4(bootTime), null, false);
    }

    public static nfs_resop4 execute(CompoundContext context, AbstractNFSv4Operation op) throws ChimeraNFSException, IOException {
        nfs_resop4 result = nfs_resop4.resopFor(op._args.argop);
        op.process(context, result);
        return result;
    }

    public void executeWithStatus(CompoundContext context, AbstractNFSv4Operation op, int status) throws ChimeraNFSException, IOException {
        nfs_resop4 result = nfs_resop4.resopFor(op._args.argop);
        int currentStatus = nfsstat.NFS_OK;
        try {
            op.process(context, result);
        } catch (ChimeraNFSException e) {
            currentStatus = e.getStatus();
        }
        AssertNFS.assertNFSStatus(status, currentStatus);
    }

    public static nfs_fh4 generateFileHandle() {
        byte[] b = new byte[nfs4_prot.NFS4_FHSIZE];
        RANDOM.nextBytes(b);
        return new nfs_fh4(b);
    }

    public static RpcCall generateRpcCall() {

        RpcAuth auth = new RpcAuthTypeUnix(1, 2, new int[] {3, 4}, 1, "localhost");

        RpcTransport transport = mock(RpcTransport.class);
        given(transport.getLocalSocketAddress()).willReturn(new InetSocketAddress(2049));
        given(transport.getRemoteSocketAddress()).willReturn(new InetSocketAddress(RANDOM.nextInt(65535)));

        RpcCall call = mock(RpcCall.class);
        given(call.getCredential()).willReturn(auth);
        given(call.getTransport()).willReturn(transport);

        return call;
    }
}
