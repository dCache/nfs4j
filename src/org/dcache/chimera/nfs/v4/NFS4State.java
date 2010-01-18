/*
 * $Id:NFS4State.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

class NFS4State {            
    
    
    /*
                  struct stateid4 {
                    uint32_t        seqid;
                    opaque          other[12];
                  };

           This structure is used for the various state sharing mechanisms
           between the client and server.  For the client, this data structure
           is read-only.  The starting value of the seqid field is undefined.
           The server is required to increment the seqid field monotonically at
           each transition of the stateid.  This is important since the client
           will inspect the seqid in OPEN stateids to determine the order of
           OPEN processing done by the server.
   
     */
		        	
    private int _seqid = 0;
    private byte[] _other = new byte[12];
    private boolean _isConfimed = false;
    
    public NFS4State(int seqid) {
        
        byte[] other_local = Integer.toString( this.hashCode()).getBytes();
        
        int len = other_local.length > 12 ? 12 : other_local.length;        
        System.arraycopy(other_local, 0, _other, 0, len);
        for( int i = 0; i  < 12 - len; i++ ) {
        	_other[len + i] = '0';        	
        }
      
        _seqid = seqid;
        
    }            
    
    public int seqid() { return _seqid; }
    public void bumpSeqid() { ++ _seqid; }
    
   
    
    public byte []  other() {
        return _other;
    }
    
    
    public void confirm() {
    	_isConfimed = true;
    }
    
    public boolean isConfimed() {
    	return _isConfimed;
    }
}


/*
 * $Log: NFS4State.java,v $
 * Revision 1.4  2006/07/04 15:29:35  tigran
 * fixed  creation of stateid
 *
 * Revision 1.3  2006/07/04 15:17:49  tigran
 * initial stateid provided by client
 *
 * Revision 1.2  2006/07/04 14:46:12  tigran
 * basic state handling
 *
 * Revision 1.1  2006/06/27 16:29:37  tigran
 * first touch to states
 * TODO: it does not work yet!
 *
 */