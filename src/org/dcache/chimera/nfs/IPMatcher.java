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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPMatcher {


    // FIXME: make it more elegant
    public static boolean match(String pattern, InetAddress ip) {


        if( pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {
            // regexp
            String hostName = ip.getHostName();

            return match(pattern, hostName);

        }else{
            // ip
            try {

                int mask = 32;
                String ipMask[] = pattern.split("/");
                if(ipMask.length > 2 ) {
                    // invalid record - deny
                    return false;
                }

                if(ipMask.length == 2) {
                    mask = Integer.parseInt(ipMask[1]);
                }

                return match(InetAddress.getByName(ipMask[0]), ip, mask);
            }catch(UnknownHostException uhe) {
                return false;
            }
        }
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
         * if there are no partial defined bytes we are done, other vise check them.
         */
        return lastBits == 0 ? true :
                (ipBytes[fullBytes] >> (8 - lastBits)) == (netBytes[fullBytes] >> (8 - lastBits));
    }


    private static String toRegExp(String s) {
                return s.replace(".", "\\.").replace("?", ".").replace("*", ".*");
    }
}
