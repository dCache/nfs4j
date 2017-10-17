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

    protected LockManager nlm;

    @Before
    public void setUp() throws Exception {
        nlm = new SimpleLm();
    }

    @Test
    public void shouldAllowFreshLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();
    }

    @Test(expected = LockDeniedException.class)
    public void shouldFailOnConflictingLockDifferentOwner() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .read()
                .lock();

        given().owner("owner2")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();
    }

    @Test
    public void shouldAllowConflictingLockSameOwner() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .read()
                .lock();

        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();
    }

    @Test
    public void shouldAllowNonConflictingLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .read()
                .lock();

        given().owner("owner2")
                .on("file1")
                .from(2)
                .length(1)
                .write()
                .lock();
    }

    @Test
    public void shouldAllowConflictingLocksOnDifferentFiles() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();

        given().owner("owner2")
                .on("file2")
                .from(0)
                .length(1)
                .write()
                .lock();
    }

    @Test(expected = LockRangeUnavailabeException.class)
    public void shouldFailOnNonMatchingLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .read()
                .unlock();
    }

    @Test
    public void shouldAllowLockAfterUnlock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();

        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .unlock();

        given().owner("owner2")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();
    }

    @Test
    public void shouldAllowTestOnNonExinstingLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .test();
    }

    @Test(expected = LockDeniedException.class)
    public void shouldFailTestOnExinstingLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .read()
                .lock();

        given().owner("owner2")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .test();
    }

    @Test
    public void shouldAllowNonOverlapingLocks() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();

        given().owner("owner2")
                .on("file1")
                .from(1)
                .length(nfs4_prot.NFS4_UINT64_MAX) // up-to the end
                .write()
                .lock();
    }

    @Test(expected = LockDeniedException.class)
    public void shouldFailWhenLockedUpToTheEnd() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(nfs4_prot.NFS4_UINT64_MAX) // up-to the end
                .write()
                .lock();

        given().owner("owner2")
                .on("file1")
                .from(1)
                .length(1)
                .read()
                .lock();
    }


    @Test
    public void shouldAllowTestAfterUnLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .lock();

        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .unlock();

        given().owner("owner2")
                .on("file1")
                .from(0)
                .length(1)
                .write()
                .test();
    }

    @Test
    public void shouldAllowTestInLockedRegion() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(100)
                .length(50)
                .write()
                .lock();

        given().owner("owner1")
                .on("file1")
                .from(75)
                .length(50)
                .write()
                .test();
    }

    @Test
    public void shouldAllowSplitLock() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(0)
                .length(3)
                .write()
                .lock();

        given().owner("owner1")
                .on("file1")
                .from(1)
                .length(1)
                .write()
                .unlock();

        given().owner("owner1")
                .on("file1")
                .from(1)
                .length(1)
                .write()
                .test();
    }

    @Test(expected = LockDeniedException.class)
    public void shouldMergeOverlapingLocks() throws LockException {
        given().owner("owner1")
                .on("file1")
                .from(25)
                .length(75)
                .write()
                .lock();

        given().owner("owner1")
                .on("file1")
                .from(50)
                .length(100)
                .write()
                .lock();


        given().owner("owner2")
                .on("file1")
                .from(0)
                .length(-1)
                .write()
                .test();
    }

    private LockBuilder given() {
        return new LockBuilder();
    }

    private class LockBuilder {

        private byte[] file;
        private long offset;
        private long length;
        private StateOwner owner;
        private int lockType;

        LockBuilder on(String file) {
            this.file = file.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        LockBuilder owner(String owner) {
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

        LockBuilder read() {
            this.lockType = nfs_lock_type4.READ_LT;
            return this;
        }

        LockBuilder write() {
            this.lockType = nfs_lock_type4.WRITE_LT;
            return this;
        }

        void lock() throws LockException {
            NlmLock lock = new NlmLock(owner, lockType, offset, length);
            nlm.lock(file, lock);
        }

        void unlock() throws LockException {
            NlmLock lock = new NlmLock(owner, lockType, offset, length);
            nlm.unlock(file, lock);
        }

        void test() throws LockException {
            NlmLock lock = new NlmLock(owner, lockType, offset, length);
            nlm.test(file, lock);
        }

    }
}
