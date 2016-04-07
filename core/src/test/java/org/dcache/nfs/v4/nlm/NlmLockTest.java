package org.dcache.nfs.v4.nlm;

import java.nio.charset.StandardCharsets;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.lock_owner4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class NlmLockTest {

    private lock_owner4 lo1;
    private lock_owner4 lo2;

    @Before
    public void setUp() {
        state_owner4 so1 = new state_owner4();
        so1.owner = "lock_owner1".getBytes(StandardCharsets.UTF_8);
        so1.clientid = new clientid4(1L);

        state_owner4 so2 = new state_owner4();
        so2.owner = "lock_owner2".getBytes(StandardCharsets.UTF_8);
        so2.clientid = new clientid4(2L);

        lo1 = new lock_owner4(so1);
        lo2 = new lock_owner4(so2);
    }

    @Test
    public void shouldNotConflictFromSameOwnerSameType() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("locks from the same owner must not conflict", lock1.isConflicting(lock2));
    }

    @Test
    public void shouldNotConflictFromSameOwnerDifferentType() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("locks from the same owner must not conflict", lock1.isConflicting(lock2));
    }

    @Test
    public void shouldNotConflictReadLocksFromDifferentOwner() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo2, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("read locks must not conflict", lock1.isConflicting(lock2));
    }

    @Test
    public void shouldConflictReadWriteFromDifferentOwner() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo2, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertTrue("read locks must not conflict", lock1.isConflicting(lock2));
    }

    @Test
    public void shouldConflictOnReadWriteLocks() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo2, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertTrue("read+write locks must conflict", lock1.isConflictingType(lock2));
    }

    @Test
    public void shouldDetectDifferentOwners() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo2, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("must detect different owners", lock1.isSameOwner(lock2));
    }

    @Test
    public void shouldDetectSameOwner() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertTrue("must detect same owners", lock1.isSameOwner(lock2));
    }

}
