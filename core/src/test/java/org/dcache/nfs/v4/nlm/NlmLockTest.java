package org.dcache.nfs.v4.nlm;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.dcache.nfs.v4.StateOwner;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class NlmLockTest {

    private StateOwner lo1;
    private StateOwner lo2;

    @Before
    public void setUp() {
        state_owner4 so1 = new state_owner4();
        so1.owner = "lock_owner1".getBytes(StandardCharsets.UTF_8);
        so1.clientid = new clientid4(1L);

        state_owner4 so2 = new state_owner4();
        so2.owner = "lock_owner2".getBytes(StandardCharsets.UTF_8);
        so2.clientid = new clientid4(2L);

        lo1 = new StateOwner(so1, 1);
        lo2 = new StateOwner(so2, 1);
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

    @Test
    public void shouldDetectConflictingRangeLock() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 1, 9223372036854775807L);

        assertTrue("overlaping lock range not detected", lock1.isOverlappingRange(lock2));
    }

    @Test
    public void shouldAllowEndingLockRange() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, 1);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 2, 1);
        NlmLock lock = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 3, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("false conflicting lock range", lock.isOverlappingRange(lock1));
        assertFalse("false conflicting lock range", lock.isOverlappingRange(lock2));
    }

    @Test
    public void shouldEqual() {
        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, 1);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, 1);

        assertTrue("the same lock should be equal", lock1.equals(lock2));
        assertTrue("equal objects must have the same hashcode", lock1.hashCode() == lock2.hashCode());
    }

    @Test
    public void shouldNotBeEqualByOwner() {

        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo2, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("lock with different owner can't be equal", lock1.equals(lock2));
    }

    @Test
    public void shouldNotBeEqualByOffset() {

        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 1, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("lock with different offsets can't be equal", lock1.equals(lock2));
    }

    @Test
    public void shouldNotBeEqualByType() {

        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, nfs4_prot.NFS4_UINT64_MAX);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.READ_LT, 0, nfs4_prot.NFS4_UINT64_MAX);

        assertFalse("lock with different types can't be equal", lock1.equals(lock2));
    }

    @Test
    public void shouldNotBeEqualByLegth() {

        NlmLock lock1 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, 1);
        NlmLock lock2 = new NlmLock(lo1, nfs_lock_type4.WRITE_LT, 0, 2);

        assertFalse("lock with different length can't be equal", lock1.equals(lock2));
    }
}
