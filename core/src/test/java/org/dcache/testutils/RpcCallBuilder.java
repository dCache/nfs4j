package org.dcache.testutils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.dcache.nfs.v3.xdr.nfs3_prot;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcAuthTypeNone;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;
import org.mockito.Mockito;

public class RpcCallBuilder {
    private InetAddressBuilder sourceAddressBuilder = new InetAddressBuilder();
    private int sourcePort;
    private int program;
    private int version;
    private RpcAuth rpcAuth;

    public RpcCallBuilder nfs3() {
        this.program = nfs3_prot.NFS_PROGRAM;
        this.version = nfs3_prot.NFS_V3;
        return this;
    }

    public RpcCallBuilder from(String sourceIpAddress, String sourceHostName, int sourcePort) {
        sourceAddressBuilder.ip(sourceIpAddress).hostName(sourceHostName);
        this.sourcePort = sourcePort;
        return this;
    }

    public RpcCallBuilder noAuth() {
        rpcAuth = new RpcAuthTypeNone();
        return this;
    }

    public RpcCall build() {
        InetAddress clientAddress = sourceAddressBuilder.build();
        InetSocketAddress socketAddress = new InetSocketAddress(clientAddress, sourcePort);
        RpcTransport transport = Mockito.mock(RpcTransport.class);
        Mockito.when(transport.getRemoteSocketAddress()).thenReturn(socketAddress);
        return new RpcCall(program, version, rpcAuth, transport);
    }
}
