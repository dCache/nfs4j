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
package org.dcache.nfs.v4.client;
import java.io.IOException;

import java.net.InetAddress;
import javax.security.auth.Subject;
import org.dcache.auth.Subjects;

import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.oncrpc4j.rpc.OncRpcClient;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcAuthTypeUnix;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;
import org.dcache.oncrpc4j.xdr.XdrVoid;
import org.dcache.utils.UnixUtils;


/**
 * The class <code>nfs4_prot_NFS4_PROGRAM_Client</code> implements the client stub proxy
 * for the NFS4_PROGRAM remote program. It provides method stubs
 * which, when called, in turn call the appropriate remote method (procedure).
 */
public class nfs4_prot_NFS4_PROGRAM_Client {

    private final RpcCall client;
    private final OncRpcClient rpcClient;

    /**
     * Constructs a <code>nfs4_prot_NFS4_PROGRAM_Client</code> client stub proxy object
     * from which the NFS4_PROGRAM remote program can be accessed.
     * @param host Internet address of host where to contact the remote program.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *   used for ONC/RPC calls.
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public nfs4_prot_NFS4_PROGRAM_Client(InetAddress host, int protocol)
           throws OncRpcException, IOException {
           this(host, 2049, protocol);
    }

    /**
     * Constructs a <code>nfs4_prot_NFS4_PROGRAM_Client</code> client stub proxy object
     * from which the NFS4_PROGRAM remote program can be accessed.
     * @param host Internet address of host where to contact the remote program.
     * @param port Port number at host where the remote program can be reached.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *   used for ONC/RPC calls.
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public nfs4_prot_NFS4_PROGRAM_Client(InetAddress host, int port, int protocol)
           throws OncRpcException, IOException {

        Subject currentUser = UnixUtils.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("unable to determine current unix user. please provide uid/gid explicitly");
        }

        int uid = (int)Subjects.getUid(currentUser);
        int gid = (int)Subjects.getPrimaryGid(currentUser);
        int[] gids = UnixUtils.toIntArray(Subjects.getGids(currentUser));

        rpcClient = new OncRpcClient(host, protocol, port);
        RpcTransport transport;
        transport = rpcClient.connect();

        RpcAuth credential = new RpcAuthTypeUnix(uid, gid, gids,
            (int) (System.currentTimeMillis() / 1000),
            InetAddress.getLocalHost().getHostName());
        client = new RpcCall(100003, 4, credential, transport);
    }

    /**
     * Constructs a <code>nfs4_prot_NFS4_PROGRAM_Client</code> client stub proxy object
     * from which the NFS4_PROGRAM remote program can be accessed.
     * @param host Internet address of host where to contact the remote program.
     * @param port Port number at host where the remote program can be reached.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *   used for ONC/RPC calls.
     * @param gid gid to authenticate as
     * @param uid uid to authenticate as
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public nfs4_prot_NFS4_PROGRAM_Client(InetAddress host, int port, int protocol, int uid, int gid)
            throws OncRpcException, IOException {
        rpcClient = new OncRpcClient(host, protocol, port);
        RpcTransport transport;
        transport = rpcClient.connect();
        RpcAuth credential = new RpcAuthTypeUnix(
                uid, gid, new int[]{gid},
                (int) (System.currentTimeMillis() / 1000),
                InetAddress.getLocalHost().getHostName());
        client = new RpcCall(100003, 4, credential, transport);
    }

    /**
     * Constructs a <code>nfs4_prot_NFS4_PROGRAM_Client</code> client stub proxy object
     * from which the NFS4_PROGRAM remote program can be accessed.
     * @param client ONC/RPC client connection object implementing a particular
     *   protocol.
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
//    public nfs4_prot_NFS4_PROGRAM_Client(OncRpcClient client)
//           throws OncRpcException, IOException {
//        super(client);
//    }

    /**
     * Constructs a <code>nfs4_prot_NFS4_PROGRAM_Client</code> client stub proxy object
     * from which the NFS4_PROGRAM remote program can be accessed.
     * @param host Internet address of host where to contact the remote program.
     * @param program Remote program number.
     * @param version Remote program version number.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *   used for ONC/RPC calls.
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
//    public nfs4_prot_NFS4_PROGRAM_Client(InetAddress host, int program, int version, int protocol)
//           throws OncRpcException, IOException {
//        super(host, program, version, 0, protocol);
//    }

    /**
     * Constructs a <code>nfs4_prot_NFS4_PROGRAM_Client</code> client stub proxy object
     * from which the NFS4_PROGRAM remote program can be accessed.
     * @param host Internet address of host where to contact the remote program.
     * @param program Remote program number.
     * @param version Remote program version number.
     * @param port Port number at host where the remote program can be reached.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *   used for ONC/RPC calls.
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
//    public nfs4_prot_NFS4_PROGRAM_Client(InetAddress host, int program, int version, int port, int protocol)
//           throws OncRpcException, IOException {
//        super(host, program, version, port, protocol);
//    }

    /**
     * Call remote procedure NFSPROC4_NULL_4.
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public void NFSPROC4_NULL_4()
           throws OncRpcException, IOException {
        XdrVoid args$ = XdrVoid.XDR_VOID;
        XdrVoid result$ = XdrVoid.XDR_VOID;

        client.call(nfs4_prot.NFSPROC4_NULL_4, args$, result$);
    }

    /**
     * Call remote procedure NFSPROC4_COMPOUND_4.
     * @param arg1 parameter (of type COMPOUND4args) to the remote procedure call.
     * @return Result from remote procedure call (of type COMPOUND4res).
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException if an I/O error occurs.
     */
    public COMPOUND4res NFSPROC4_COMPOUND_4(COMPOUND4args arg1)
           throws OncRpcException, IOException {
        COMPOUND4res result$ = new COMPOUND4res();

        client.call(nfs4_prot.NFSPROC4_COMPOUND_4, arg1, result$);

        return result$;
    }

    public RpcTransport getTransport() {
        return client.getTransport();
    }

    public void close() throws IOException {
        rpcClient.close();
    }
}
// End of nfs4_prot_NFS4_PROGRAM_Client.java
