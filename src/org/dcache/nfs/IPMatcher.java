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
package org.dcache.nfs;

import static com.google.common.primitives.Ints.fromByteArray;
import static com.google.common.primitives.Longs.fromBytes;
import static com.google.common.base.Preconditions.checkArgument;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPMatcher {


    private static final int IPv4_FULL_MASK = 32;
    private static final int IPv6_FULL_MASK = 128;
    private static final int IPv6_HALF_MASK = 64;
    /**
     * Match a String pattern with given IP address.
     * The provided pattern can be in the the following form:
     * <pre>
     *     ipv4: a.b.c.b[/mask],
     *          for example 192.168.2.124 or 192.168.2.0/24
     *     ipv6: a:b:c:d:e:f[/mask],
     *          for example fe80::21c:c0ff:fea0:caf4 or fe80::21c:c0ff:fea0:caf4/64
     *     host name:
     *          some.host.name, for example www.dcache.org
     *     patterns like:
     *          *.dcache.org
     *          node?.dcache.org
     * </pre>
     * @param pattern to match
     * @param ip to match pattern with.
     * @return @{code true} if given ip matches provided pattern.
     */
    public static boolean match(String pattern, InetAddress ip) {

        if( pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1 ){
            // regexp
            String hostName = ip.getHostName();

            return match(pattern, hostName);
        }

        String ipMask[] = pattern.split("/");
        if( ipMask.length > 2 ){
            // invalid record - deny
            return false;
        }

        /*
         * if subnet mask defined
         */
        int mask;
        if( ipMask.length == 2 ){
            mask = Integer.parseInt(ipMask[1]);
        } else {
            mask = fullMaskOf(ip);
        }

        InetAddress[] addresses;
        try{
            addresses = InetAddress.getAllByName(ipMask[0]);
        }catch( UnknownHostException uhe ){
            return false;
        }

        for(InetAddress address : addresses){
            if( match(address, ip, mask) )
                return true;
        }
        return false;
    }


    public static boolean match(String pattern, String hostName ) {

        Pattern p = Pattern.compile(toRegExp(pattern));
        Matcher m = p.matcher(hostName);

        return m.matches();
    }


    /**
     * Checks matching ip in specified subnet.
     *
     * @param ip address to test
     * @param subnet address
     * @param mask netmask
     * @return true if ip matches subnet.
     */
    public static boolean match(InetAddress ip, InetAddress subnet, int mask) {

        checkArgument(mask >= 0, "Netmask should be positive");
        byte[] ipBytes = ip.getAddress();
        byte[] netBytes = subnet.getAddress();

        if (ipBytes.length != netBytes.length) {
            return false;
        }

        if (ipBytes.length == 4) {
            checkArgument(mask <= IPv4_FULL_MASK,
                    "Netmask for ipv4 can't be bigger than" + IPv4_FULL_MASK);
            /*
             * IPv4 can be represented as a 32 bit ints.
             */
            int ipAsInt = fromByteArray(ipBytes);
            int netAsBytes = fromByteArray(netBytes);

            return (ipAsInt ^ netAsBytes) >> (IPv4_FULL_MASK - mask) == 0;
        }

        checkArgument(mask <= IPv6_FULL_MASK,
                "Netmask for ipv6 can't be bigger than" + IPv6_FULL_MASK);

        /**
         * IPv6 can be represented as two 64 bit longs.
         *
         * We evaluate second long only if bitmask bigger than 64.
         * The second longs are created only if needed as it turned to be
         * the slowest part.
         */
        long ipAsLong0 = fromBytes( ipBytes[0], ipBytes[1], ipBytes[2], ipBytes[3],
                ipBytes[4], ipBytes[5], ipBytes[6], ipBytes[7]);
        long netAsLong0 = fromBytes(netBytes[0], netBytes[1], netBytes[2], netBytes[3],
                netBytes[4], netBytes[5], netBytes[6], netBytes[7]);

        if(mask > 64) {
            long ipAsLong1 = fromBytes(ipBytes[8], ipBytes[9], ipBytes[10], ipBytes[11],
                ipBytes[12], ipBytes[13], ipBytes[14], ipBytes[15]);

            long netAsLong1 = fromBytes(netBytes[8], netBytes[9], netBytes[10], netBytes[11],
                    netBytes[12], netBytes[13], netBytes[14], netBytes[15]);

            return (ipAsLong0 == netAsLong0) &
            (ipAsLong1 ^ netAsLong1) >> (IPv6_FULL_MASK - mask) == 0;
        }
        return (ipAsLong0 ^ netAsLong0) >> (IPv6_HALF_MASK - mask) == 0;
    }

    private static String toRegExp(String s) {
                return s.replace(".", "\\.").replace("?", ".").replace("*", ".*");
    }

    private static int fullMaskOf(InetAddress address) {
        if(address instanceof Inet4Address)
            return IPv4_FULL_MASK;
        if(address instanceof Inet6Address)
            return IPv6_FULL_MASK;
        throw new IllegalArgumentException("Unsupported Inet type: " + address.getClass().getName());
    }
}
