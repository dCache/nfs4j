/*
 * $Id:NameFilter.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.*;
import org.dcache.chimera.nfs.ChimeraNFSException;

class NameFilter {
	
	/**
     *
	 * validate name and return an instance of string or throw exception
     *
	 * @param bytes
	 * @return
     * @throws ChimeraNFSException
    */
    public static String convert(byte [] bytes) throws ChimeraNFSException {
		
		String ret = null;
		
        if( bytes.length == 0 ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "zero-length name");
        }            

		try {
			ret =  new String( bytes, "UTF-8");
		}catch(Exception e) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "invalid utf8 name");
		}

		return ret;
	}

}
