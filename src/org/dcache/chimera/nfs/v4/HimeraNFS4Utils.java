/*
 * $Id:HimeraNFS4Utils.java 140 2007-06-07 13:44:55Z tigran $
 */

package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsace4;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cis;
import org.dcache.chimera.nfs.v4.xdr.utf8string;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.RpcAuthTypeUnix;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.nfs.ExportFile;


public class HimeraNFS4Utils {

    private HimeraNFS4Utils(){ /* no instance allowed */}

    public static org.dcache.chimera.posix.UnixUser remoteUser(RpcCall call, ExportFile exports) {

        org.dcache.chimera.posix.UnixUser user = null;
        int uid = -1;
        int gid = -1;
        int[] gids = null;

        if( call.getCredential().type() == RpcAuthType.UNIX) {

            uid = ((RpcAuthTypeUnix)call.getCredential()).uid();
            gid = ((RpcAuthTypeUnix)call.getCredential()).gid();
            gids = ((RpcAuthTypeUnix)call.getCredential()).gids();
        }

        String host = call.getTransport().getRemoteSocketAddress().getAddress().getHostName();



        // root access only for trusted hosts
        if( uid == 0 ) {
            if( (exports == null) || !exports.isTrusted(call.getTransport().getRemoteSocketAddress().getAddress()) ) {

                // FIXME: actual 'nobody' account should be used
                uid = -1;
                gid = -1;
            }
        }

        user = new org.dcache.chimera.posix.UnixUser(uid, gid, gids, host);

        return user;

    }


    public static String inetAddress2rAddr( InetSocketAddress address)  {


        byte[] host_part = address.getAddress().getAddress();

        int port = address.getPort();
        int port_part[] = new int[2];
        port_part[0] = (port & 0xff00) >> 8;
        port_part[1] = port & 0x00ff;

        StringBuilder sb = new StringBuilder();
        sb.append(0xFF & host_part[0]).append(".");
        sb.append(0xFF & host_part[1]).append(".");
        sb.append(0xFF & host_part[2]).append(".");
        sb.append(0xFF & host_part[3]).append(".");
        sb.append(0xFF & port_part[0]).append(".");
        sb.append(0xFF & port_part[1]);

        return sb.toString();

    }

    public static String inetAddress2rAddr( String host, int port) throws UnknownHostException  {

        return inetAddress2rAddr( new InetSocketAddress(host, port));
    }



    public static String aceToString(nfsace4 ace) {

    	String who = new String(ace.who.value.value);
    	int type = ace.type.value.value;
    	int flag = ace.flag.value.value;
    	int mask = ace.access_mask.value.value;




    	return who + " " + aceType2String(type) + " " + Integer.toBinaryString(flag) + " " + Integer.toBinaryString(mask);

    }


    private static String aceType2String(int type) {

    	switch(type) {

    	case nfs4_prot.ACE4_ACCESS_ALLOWED_ACE_TYPE:
    		return "ALLOW";
    	case nfs4_prot.ACE4_ACCESS_DENIED_ACE_TYPE:
    		return "DENY ";
    	case nfs4_prot.ACE4_SYSTEM_ALARM_ACE_TYPE:
    		return "ALARM";
    	case nfs4_prot.ACE4_SYSTEM_AUDIT_ACE_TYPE:
    		return "AUDIT";
    	default:
    		return ">BAD<";
    	}

    }

    /**
     * Convert String to a case sensitive string of UTF-8 characters.
     * @param str
     * @return utf8str_cs representation of <i>str</i>
     */
    public static utf8str_cs string2utf8str_cs(String str) {
        return new utf8str_cs( new utf8string(str.getBytes()));
    }

    /**
     * Convert String to a case insensitive string of UTF-8 characters.
     * @param str
     * @return utf8str_cis representation of <i>str</i>
     */
    public static utf8str_cis string2utf8str_cis(String str) {
        return new utf8str_cis( new utf8string(str.getBytes()));
    }

}
