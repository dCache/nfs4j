package org.dcache.nfs.v4.nlm;

import java.nio.charset.StandardCharsets;
import org.dcache.nfs.v4.StateOwner;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.junit.Test;
import org.junit.Before;

/**
 *
 */
public class SimpleLmTest {

    private LockManager nlm;
    private byte[] file1;
    private byte[] file2;

    @Before
    public void setUp() throws Exception {
        nlm = new SimpleLm();
        file1 = "file1".getBytes(StandardCharsets.UTF_8);
        file2 = "file2".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void testAllowFreshLock() throws LockException {
        NlmLock lock = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forWrite()
                .build();

        nlm.lock(file1, lock);
    }

    @Test(expected = LockDeniedException.class)
    public void testConflictingLockByDifferentOwner() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        nlm.lock(file1, lock2);
    }

    @Test
    public void testConflictingLockSameOwner() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        nlm.lock(file1, lock2);
    }

    @Test
    public void testNonConflictingLock() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner2")
                .from(2)
                .length(1)
                .forWrite()
                .build();
        nlm.lock(file1, lock2);
    }

    @Test
    public void testOverlapingLocksOnDifferentFiles() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        nlm.lock(file2, lock2);
    }

    @Test(expected = LockRangeUnavailabeException.class)
    public void testUnlockOfNonMatchingLock() throws LockException {
        NlmLock lock = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.unlock(file1, lock);
    }

    @Test
    public void testLockAfterUnlock() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        nlm.unlock(file1, lock2);

        NlmLock lock3 = new LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        nlm.lock(file1, lock3);
    }

    @Test
    public void testTestOfNonExinstingLock() throws LockException {
        NlmLock lock = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.test(file1, lock);
    }

    @Test(expected = LockDeniedException.class)
    public void testTestOfExinstingLock() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        nlm.test(file1, lock2);
    }

    @Test
    public void testNonOverlapingLocks() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner2")
                .from(1)
                .length(nfs4_prot.NFS4_UINT64_MAX)
                .forWrite()
                .build();
        nlm.lock(file1, lock2);
    }

    @Test(expected = LockDeniedException.class)
    public void testLockUpToTheEnd() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(nfs4_prot.NFS4_UINT64_MAX)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner2")
                .from(1)
                .length(1)
                .forWrite()
                .build();
        nlm.lock(file1, lock2);
    }

    @Test
    public void testTestAfterUnLock() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.unlock(file1, lock2);

        NlmLock lock3 = new LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(1)
                .forRead()
                .build();
        nlm.test(file1, lock3);
    }

    @Test
    public void testTestInLockedRegion() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(100)
                .length(50)
                .forRead()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner1")
                .from(75)
                .length(50)
                .forWrite()
                .build();
        nlm.test(file1, lock2);
    }

    @Test
    public void testSplitLock() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(3)
                .forWrite()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner1")
                .from(1)
                .length(1)
                .forRead()
                .build();
        nlm.unlock(file1, lock2);

        NlmLock lock3 = new LockBuilder()
                .withOwner("owner2")
                .from(1)
                .length(1)
                .forWrite()
                .build();
        nlm.test(file1, lock3);
    }

    @Test(expected = LockDeniedException.class)
    public void testMergeOfOverlapingLocks() throws LockException {
        NlmLock lock1 = new LockBuilder()
                .withOwner("owner1")
                .from(25)
                .length(75)
                .forWrite()
                .build();
        nlm.lock(file1, lock1);

        NlmLock lock2 = new LockBuilder()
                .withOwner("owner1")
                .from(50)
                .length(100)
                .forWrite()
                .build();
        nlm.lock(file1, lock2);

        NlmLock lock3 = new LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(nfs4_prot.NFS4_UINT64_MAX)
                .forWrite()
                .build();
        nlm.test(file1, lock3);
    }

    public static class LockBuilder {

        private long offset;
        private long length;
        private StateOwner owner;
        private int lockType;

        LockBuilder withOwner(String owner) {
            state_owner4 so = new state_owner4();

            so.owner = owner.getBytes(StandardCharsets.UTF_8);
            so.clientid = new clientid4(1);
            this.owner = new StateOwner(so, 1);
            return this;
        }

        LockBuilder from(long offset) {
            this.offset = offset;
            return this;
        }

        LockBuilder length(long length) {
            this.length = length;
            return this;
        }

        LockBuilder forRead() {
            this.lockType = nfs_lock_type4.READ_LT;
            return this;
        }

        LockBuilder forWrite() {
            this.lockType = nfs_lock_type4.WRITE_LT;
            return this;
        }

        NlmLock build() {
           return new NlmLock(owner, lockType, offset, length);
        }
    }
}
