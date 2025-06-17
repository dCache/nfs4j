package org.dcache.testutils;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        try {
            return InetAddress.getByAddress(hostName, InetAddress.getByName(ipAddress).getAddress());
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }
}
