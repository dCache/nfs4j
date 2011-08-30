package org.dcache.utils.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;
import static org.junit.Assert.*;

public class InetSocketAddressesTest {

    @Test
    public void testLocalHostV4() throws Exception {
        String uaddr = "127.0.0.1.203.81";
        InetSocketAddress socketAddress = InetSocketAddresses.forUaddrString(uaddr);
        assertEquals("port mismatch", 52049, socketAddress.getPort());
        assertEquals("host mismatch", InetAddress.getByName("127.0.0.1"),
                socketAddress.getAddress());
    }

    @Test
    public void testLocalHostV6() throws Exception {
        String uaddr = "::1.203.81";
        InetSocketAddress socketAddress = InetSocketAddresses.forUaddrString(uaddr);
        assertEquals("port mismatch", 52049, socketAddress.getPort());
        assertEquals("host mismatch", InetAddress.getByName("::1"),
                socketAddress.getAddress());
    }

    @Test
    public void testLocalHostV4Revert() throws Exception {
        String uaddr = "127.0.0.1.203.81";
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("127.0.0.1"),52049);

        assertEquals("reverse conversion failed", uaddr,
                InetSocketAddresses.uaddrOf(socketAddress));
    }

    @Test
    public void testHostAndPortIpv4() throws Exception {
        String hostAndPort = "127.0.0.1:1111";
        InetSocketAddress address = InetSocketAddresses.inetAddressOf(hostAndPort);

        assertEquals(InetAddress.getByName("127.0.0.1"), address.getAddress());
        assertEquals(1111, address.getPort());
    }

    @Test
    public void testHostAndPortIpv6() throws Exception {
        String hostAndPort = "[fe80::21c:c0ff:fea0:caf4]:1111";
        InetSocketAddress address = InetSocketAddresses.inetAddressOf(hostAndPort);

        assertEquals( InetAddress.getByName("fe80::21c:c0ff:fea0:caf4"), address.getAddress());
        assertEquals(1111, address.getPort());
    }

}
