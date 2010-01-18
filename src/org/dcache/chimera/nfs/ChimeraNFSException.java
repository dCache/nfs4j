/*
 * $Id:ChimeraNFSException.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs;

public class ChimeraNFSException extends java.io.IOException {

    /**
     *
     */
    private static final long serialVersionUID = 4319461664218810541L;
    private final int nfsStatus;

    public ChimeraNFSException(int newStatus, String msg) {
        super(msg);
        nfsStatus = newStatus;
    }

   public int getStatus() {
       return nfsStatus;
   }
}
