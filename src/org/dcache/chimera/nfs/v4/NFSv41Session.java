package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.concurrent.atomic.AtomicLong;

public class NFSv41Session {


	private static final AtomicLong SESSIONS = new AtomicLong(0);

	private int _sequenceID = 0;
	private final byte[] _session = (Long.toString(SESSIONS.incrementAndGet()) + "###############").getBytes();

	// FIXME: here have to be real cache
	private final int _slots[] = new int[200];

	private final NFS4Client _client;

	public NFSv41Session(NFS4Client client) {
		_client = client;
		_sequenceID = _client.currentSeqID();
	}

	public byte[] id() {
		byte[] id = new byte[16];
		System.arraycopy(_session, 0, id, 0, 16);

		return id;
	}

	public int nextSequenceID() {
		return ++_sequenceID;
	}

	public int sequenceID() {
		return _sequenceID;
	}

	public NFS4Client getClient() {
		return _client;
	}


	public int slotMax() {
	    return _slots.length -1;
	}

    public void setSlot(int slot, int sequence) throws ChimeraNFSException {

	    if( slot >= _slots.length ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADSLOT, "slot id overflow");
	    }

        /*
         * According to spec.
         *
         * If the previous sequence id was 0xFFFFFFFF,
         * then the next request for the slot MUST have
         * the sequence id set to zero.
         */

	    int validValue;
	    if( _slots[slot] == 0xFFFFFFFF ) {
	        validValue = 0;
	    }else{
            validValue = _slots[slot] + 1;
	    }

        if( sequence != validValue ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_SEQ_MISORDERED, "slot["+ slot +"] disordered : v/n : " + Integer.toHexString(validValue) +"/" +Integer.toHexString(sequence) );
        }

	    _slots[slot] = sequence;
	}

    @Override
    public String toString() {
        String s = String.format("Session: [%s], sequence: [%d]", new String(_session), _sequenceID);
        return s;
    }
}
