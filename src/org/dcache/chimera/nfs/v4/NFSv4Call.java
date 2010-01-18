/*
 * $Id:NFSv4Call.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;

public class NFSv4Call {
	
	
	// utility class, no instances allowed
	
	private NFSv4Call() {}

	
    private static final String[] OP_STRING = {
        "OP_NULL",
        "OP_1 UNUSED",
        "OP_2 UNUSED",
        "OP_ACCESS",
        "OP_CLOSE",
        "OP_COMMIT",
        "OP_CREATE",
        "OP_DELEGPURGE",
        "OP_DELEGRETURN",
        "OP_GETATTR",
        "OP_GETFH",
        "OP_LINK",
        "OP_LOCK",
        "OP_LOCKT",
        "OP_LOCKU",
        "OP_LOOKUP",
        "OP_LOOKUPP",
        "OP_NVERIFY",
        "OP_OPEN",
        "OP_OPENATTR",
        "OP_OPEN_CONFIRM",
        "OP_OPEN_DOWNGRADE",
        "OP_PUTFH",
        "OP_PUTPUBFH",
        "OP_PUTROOTFH",
        "OP_READ",
        "OP_READDIR",
        "OP_READLINK",
        "OP_REMOVE",
        "OP_RENAME",
        "OP_RENEW",
        "OP_RESTOREFH",
        "OP_SAVEFH",
        "OP_SECINFO",
        "OP_SETATTR",
        "OP_SETCLIENTID",
        "OP_SETCLIENT_CONFIRM",
        "OP_VERIFY",
        "OP_WRITE",
        "OP_RELEASE_LOCKOWNER",
    	"OP_BACKCHANNEL_CTL",
    	"OP_BIND_CONN_TO_SESSION",
    	"OP_EXCHANGE_ID",
    	"OP_CREATE_SESSION",
    	"OP_DESTROY_SESSION",
    	"OP_FREE_STATEID",
    	"OP_GET_DIR_DELEGATION",
    	"OP_GETDEVICEINFO",
    	"OP_GETDEVICELIST",
    	"OP_LAYOUTCOMMIT",
    	"OP_LAYOUTGET",
    	"OP_LAYOUTRETURN",
    	"OP_SECINFO_NO_NAME",
    	"OP_SEQUENCE",
    	"OP_SET_SSV",
    	"OP_TEST_STATEID",
    	"OP_WANT_DELEGATION",
        "OP_ILLEGAL"
    };

    /**
     * 
     * @param opnum
     * @return string representation of NFSv4.1 opration defined by opnum
     */
	public static String toString(int opnum) {
	    
		String opString = null;
		int badOP = 0;
		
	    if( (opnum < 0 ) || (opnum > OP_STRING.length -1) ){
	    	badOP = opnum;
	        opnum = nfs_opnum4.OP_ILLEGAL;
	    }
	            
	    if(opnum == nfs_opnum4.OP_ILLEGAL) {
	    	opString = "OP_ILLEGAL (# " + badOP +")";
	    }else{
	    	opString = OP_STRING[opnum] + " (#" + opnum + ")";        	
	    }
	    
	    return opString;
	}
		
}
/*
 * $Log: NFSv4Call.java,v $
 * Revision 1.1  2006/09/26 12:57:43  tigran
 * operation id to string converting done by
 * NFSv4Call class
 * $
 */