/*
 * $Id:IPFilter.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v3;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPFilter {
    
    private IPFilter() {
	 // no instance allowed
    }
    
    /**
     * Checks matching ip in specified subnet.
     *  
     * @return a <code>boolean</code> indicating if the ip 
     * in specified subnet
     * 
     * @param ip address to test in subnet within subnetmask
     * 
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
    
    public static void main(String[] args) {
      try {  
          boolean rc = IPFilter.match( InetAddress.getByName("131.169.214.0") , 
                  		InetAddress.getByName("131.169.40.255") , 
                  		16);
          
          System.out.println(rc);
          
      }catch(UnknownHostException e) {
          e.printStackTrace();
      }
        
    }
}
