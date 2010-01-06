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
        int ipLong = 0;
        int netLong = 0;

        // create an integer from address bytes
        ipLong |= (255 & ipBytes[0]);
        ipLong <<= 8;
        ipLong |= (255 & ipBytes[1]);
        ipLong <<= 8;
        ipLong |= (255 & ipBytes[2]);
        ipLong <<= 8;
        ipLong |= (255 & ipBytes[3]);

        netLong |= (255 & netBytes[0]);
        netLong <<= 8;
        netLong |= (255 & netBytes[1]);
        netLong <<= 8;
        netLong |= (255 & netBytes[2]);
        netLong <<= 8;
        netLong |= (255 & netBytes[3]);

        // first mask bits from left should be the same
        ipLong  = ipLong >> (32 - mask);
        netLong = netLong >> (32 - mask);

        return ipLong ==  netLong ;
    }


    private static String toRegExp(String s) {
                return s.replace(".", "\\.").replace("?", ".").replace("*", ".*");
    }
}
