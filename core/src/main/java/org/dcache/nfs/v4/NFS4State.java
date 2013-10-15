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
package org.dcache.nfs.v4;

import java.security.SecureRandom;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.utils.Bytes;

public class NFS4State {
        
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
		        	
    private final stateid4 _stateid;
    private boolean _isConfimed = false;
    private boolean _disposed = false;

    /**
     * Random generator to generate stateids.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    public NFS4State(stateid4 stateid) {
        _stateid = stateid;

    }

    public NFS4State(long clientid, int seqid) {
        this( new stateid4(generateState(clientid), seqid));
    }

    private static byte[] generateState(long clientid) {
        byte[] other = new byte[12];
        Bytes.putLong(other, 0, clientid);
        Bytes.putInt(other, 8, RANDOM.nextInt());
        return other;
    }

    public void bumpSeqid() { ++ _stateid.seqid.value; }
    
    public stateid4 stateid() {
        return _stateid;
    }
    
    public void confirm() {
    	_isConfimed = true;
    }
    
    public boolean isConfimed() {
    	return _isConfimed;
    }

    /**
     * Release resources used by this State if not released yet.
     * Any subsequent call will have no effect.
     */
    synchronized public final void tryDispose() {
        if (!_disposed) {
            dispose();
            _disposed = true;
        }
    }

    /**
     * Release resources used by this State.
     */
    protected void dispose() {
        // NOP
    }
}
