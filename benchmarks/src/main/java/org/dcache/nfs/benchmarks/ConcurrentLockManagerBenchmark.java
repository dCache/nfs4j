package org.dcache.nfs.benchmarks;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.dcache.nfs.v4.StateOwner;
import org.dcache.nfs.v4.nlm.LockException;
import org.dcache.nfs.v4.nlm.LockManager;
import org.dcache.nfs.v4.nlm.NlmLock;
import org.dcache.nfs.v4.nlm.SimpleLm;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
public class ConcurrentLockManagerBenchmark {

    /*
     * Lock manage object. One instance per benchmark.
     */
    @State(Scope.Benchmark)
    public static class LockManagerHolder {

        private LockManager lm;

        @Setup
        public void setUp() {
            lm = new SimpleLm();
        }

        public LockManager getLockManager() {
            return lm;
        }
    }

    /*
     * File id object. One per thread. Created in advance to reduce allocation overhead.
     */
    @State(Scope.Thread)
    public static class FileHolder {

        private final Random random = ThreadLocalRandom.current();
        private final byte[] file1 = new byte[16];

        public FileHolder() {
            random.nextBytes(file1);
        }

        public byte[] getFile() {
            return file1;
        }

    }

    @Benchmark
    @Threads(16)
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public NlmLock benchmarkConcurrentLocking(LockManagerHolder lmh, FileHolder fh) throws LockException {

        NlmLock lock = new LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forWrite()
                .build();

        lmh.getLockManager().lock(fh.getFile(), lock);
        return lock;
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
