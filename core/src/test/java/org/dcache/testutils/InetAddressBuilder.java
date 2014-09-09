package org.dcache.testutils;

import java.net.InetAddress;

import org.mockito.Mockito;

public class InetAddressBuilder {
    private String ipAddress;
    private String hostName;

    public InetAddressBuilder ip(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public InetAddressBuilder hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public InetAddress build() {
        InetAddress address = Mockito.mock(InetAddress.class);
        Mockito.when(address.getHostAddress()).thenReturn(ipAddress);
        Mockito.when(address.getHostName()).thenReturn(hostName);
        return address;
    }
}
