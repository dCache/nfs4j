package org.dcache.chimera.nfs.v4;

import java.util.List;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.concurrent.atomic.AtomicLong;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;

public class NFSv41Session {


	private static final AtomicLong SESSIONS = new AtomicLong(0);

	private final byte[] _session = (Long.toString(SESSIONS.incrementAndGet()) + "###############").getBytes();
    /**
     * Session reply slots.
     */
    private final SessionSlot[] _slots;

	private final NFS4Client _client;

        public NFSv41Session(NFS4Client client, int replyCacheSize) {
		_client = client;
                _slots = new SessionSlot[replyCacheSize];
	}

	public byte[] id() {
		byte[] id = new byte[16];
		System.arraycopy(_session, 0, id, 0, 16);

		return id;
	}

	public NFS4Client getClient() {
		return _client;
	}


    /**
     * Get maximum slot id.
     * @return max slot id.
     */
    public int slotMax() {
        return _slots.length - 1;
    }

    public boolean updateSlot(int slot, int sequence, List<nfs_resop4> reply) throws ChimeraNFSException {
        return getSlot(slot).update(sequence, reply);
    }

    /**
     * Get cache slot for given id.
     * @param i
     * @return cache slot.
     * @throws ChimeraNFSException
     */
    SessionSlot getSlot(int slot) throws ChimeraNFSException {

        if (slot > slotMax()) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADSLOT, "slot id overflow");
        }

        if (_slots[slot] == null) {
            _slots[slot] = new SessionSlot();
        }

        return _slots[slot];
    }

    @Override
    public String toString() {
        String s = String.format("Session: [%s]", new String(_session));
        return s;
    }
}
