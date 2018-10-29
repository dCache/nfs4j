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
package org.dcache.nfs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.net.InetAddresses.forString;
import static com.google.common.net.InetAddresses.isInetAddress;
import static com.google.common.primitives.Ints.fromByteArray;
import static com.google.common.primitives.Longs.fromBytes;

public abstract class InetAddressMatcher implements Predicate<InetAddress> {

    private final String pattern;

    /* disable subclassing */
    protected InetAddressMatcher(String pattern) {
        this.pattern = pattern;
    }

    public boolean match(InetAddress addr) {
        return test(addr);
    }

    public String getPattern() {
        return pattern;
    }

    public static InetAddressMatcher forPattern(String s) throws UnknownHostException {

        String hostAndMask[] = s.split("/");
        checkArgument(hostAndMask.length < 3, "Invalid host specification: " + s);

        if (!isInetAddress(hostAndMask[0])) {
            checkArgument (hostAndMask.length == 1, "Invalid host specification (hostname with mask): " + s);
            if (s.indexOf('*') != -1 || s.indexOf('?') != -1) {
                return new RegexpNameMatcher(toRegExp(s));
            } else {
                return new HostNameMatcher(s);
            }
        }

        InetAddress net = forString(hostAndMask[0]);
        if (hostAndMask.length == 2) {
            /*
             * if subnet mask defined
             */
            return new IpAddressMatcher(s, net, Integer.parseInt(hostAndMask[1]));
        }

        return new IpAddressMatcher(s, net);
    }

    public static class IpAddressMatcher extends InetAddressMatcher {

        private static final int IPv4_FULL_MASK = 32;
        private static final int IPv6_FULL_MASK = 128;
        private static final int IPv6_HALF_MASK = 64;

        private final byte[] netBytes;
        private final int mask;

        private static int fullMaskOf(InetAddress address) {
            if (address instanceof Inet4Address) {
                return IPv4_FULL_MASK;
            }
            if (address instanceof Inet6Address) {
                return IPv6_FULL_MASK;
            }
            throw new IllegalArgumentException("Unsupported Inet type: " + address.getClass().getName());
        }

        public IpAddressMatcher(String pattern, InetAddress subnet) {
            this(pattern, subnet, fullMaskOf(subnet));
        }

        public IpAddressMatcher(String pattern, InetAddress subnet, int mask) {
            super(pattern);
            this.netBytes = subnet.getAddress();
            this.mask = mask;
            checkArgument(mask >= 0, "Netmask should be positive");

            if (this.netBytes.length == 4) {
                checkArgument(mask <= IPv4_FULL_MASK,
                        "Netmask for ipv4 can't be bigger than" + IPv4_FULL_MASK);
            } else {
                checkArgument(mask <= IPv6_FULL_MASK,
                        "Netmask for ipv6 can't be bigger than" + IPv6_FULL_MASK);
            }
        }

        @Override
        public boolean test(InetAddress ip) {

            byte[] ipBytes = ip.getAddress();
            if (ipBytes.length != netBytes.length) {
                return false;
            }

            if (ipBytes.length == 4) {
                /*
                 * IPv4 can be represented as a 32 bit ints.
                 */
                int ipAsInt = fromByteArray(ipBytes);
                int netAsBytes = fromByteArray(netBytes);

                return (ipAsInt ^ netAsBytes) >> (IPv4_FULL_MASK - mask) == 0;
            }

            /**
             * IPv6 can be represented as two 64 bit longs.
             *
             * We evaluate second long only if bitmask bigger than 64. The
             * second longs are created only if needed as it turned to be the
             * slowest part.
             */
            long ipAsLong0 = fromBytes(ipBytes[0], ipBytes[1], ipBytes[2], ipBytes[3],
                    ipBytes[4], ipBytes[5], ipBytes[6], ipBytes[7]);
            long netAsLong0 = fromBytes(netBytes[0], netBytes[1], netBytes[2], netBytes[3],
                    netBytes[4], netBytes[5], netBytes[6], netBytes[7]);

            if (mask > 64) {
                long ipAsLong1 = fromBytes(ipBytes[8], ipBytes[9], ipBytes[10], ipBytes[11],
                        ipBytes[12], ipBytes[13], ipBytes[14], ipBytes[15]);

                long netAsLong1 = fromBytes(netBytes[8], netBytes[9], netBytes[10], netBytes[11],
                        netBytes[12], netBytes[13], netBytes[14], netBytes[15]);

                return (ipAsLong0 == netAsLong0)
                        & (ipAsLong1 ^ netAsLong1) >> (IPv6_FULL_MASK - mask) == 0;
            }
            return (ipAsLong0 ^ netAsLong0) >> (IPv6_HALF_MASK - mask) == 0;
        }
    }

    private static String toRegExp(String s) {
        return s.replace(".", "\\.").replace("?", ".").replace("*", ".*");
    }

    public static class RegexpNameMatcher extends InetAddressMatcher {

        private final Pattern regexpPattern;

        public RegexpNameMatcher(String pattern) {
            super(pattern);
            this.regexpPattern = Pattern.compile(pattern);
        }

        @Override
        public boolean test(InetAddress ip) {
            return regexpPattern.matcher(ip.getHostName()).matches();
        }
    }

    public static class HostNameMatcher extends InetAddressMatcher {

        HostNameMatcher(String hostname) throws UnknownHostException {
            super(hostname);
        }

        @Override
        public boolean test(InetAddress ip) {
            try {
                // jvm caches DNS results. We are fine to query on each request
                InetAddress[] addrs = InetAddress.getAllByName(getPattern());
                for(InetAddress addr: addrs) {
                    if (addr.equals(ip)) {
                        return true;
                    }
                }
            }catch (UnknownHostException e) {
                return false;
            }

            return false;
        }
    }
}
