/*
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

package org.dcache.chimera.nfs;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPMatcher {


    private static final int IPv4_FULL_MASK = 32;
    private static final int IPv6_FULL_MASK = 128;

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
    public static boolean match( InetAddress ip, InetAddress subnet, int mask ) {

        byte[] ipBytes = ip.getAddress();
        byte[] netBytes = subnet.getAddress();

        int maskLen = ipBytes.length * 8;
        if(mask > maskLen || mask < 0)
            throw new IllegalArgumentException("Invalid mask: " + mask);

        /*
         * check that all full bytes matching and then compare last bits, e.g.
         * for netmask /25 compare that first 3 bytes are equal and then check
         * last bit.
         */
        int fullBytes = (mask / 8);
        for(int i = 0; i < fullBytes; i++) {
            if(ipBytes[i] != netBytes[i])
                return false;
        }

        int lastBits = mask % 8;

        /*
         * if there are no partial defined bytes we are done, otherwise check them.
         */
        return lastBits == 0 ||
                (ipBytes[fullBytes] >> (8 - lastBits)) == (netBytes[fullBytes] >> (8 - lastBits));
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
