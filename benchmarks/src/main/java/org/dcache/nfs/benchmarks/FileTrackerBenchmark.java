package org.dcache.nfs.benchmarks;


import org.dcache.nfs.v4.FileTracker;
import org.dcache.nfs.v4.NFS4Client;
import org.dcache.nfs.v4.NFSv4StateHandler;
import org.dcache.nfs.v4.StateOwner;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.oncrpc4j.util.Bytes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;

@BenchmarkMode(Mode.Throughput)
public class FileTrackerBenchmark {

    /*
     * FileTracker object. One instance per benchmark.
     */
    @State(Scope.Benchmark)
    public static class FileTrackerHolder {

        private FileTracker fileTracker;
        private NFSv4StateHandler sh;

        @Setup
        public void setUp() {
            fileTracker = new FileTracker();
            sh = new NFSv4StateHandler();
        }

        public FileTracker getFileTracker() {
            return fileTracker;
        }

        public NFSv4StateHandler getStateHandler() {
            return sh;
        }
    }


    @Benchmark
    @Threads(48)
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public NFS4Client fileTrackerHashMapTest(FileTrackerHolder fileTrackerHolder) throws Exception {

        NFS4Client client = createClient(fileTrackerHolder.getStateHandler());
        StateOwner stateOwner = client.getOrCreateOwner(Thread.currentThread().getName().getBytes(StandardCharsets.UTF_8), new seqid4(0));
        Inode inode = generateFileHandle();
        fileTrackerHolder.getFileTracker().addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ, 0);
        fileTrackerHolder.getStateHandler().removeClient(client);
        return client;
    }


    static NFS4Client createClient(NFSv4StateHandler stateHandler) throws UnknownHostException {
        return createClient(stateHandler, 1);
    }

    static NFS4Client createClient(NFSv4StateHandler stateHandler, int minor) throws UnknownHostException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(null), 123);
        byte[] owner = new byte[8];
        byte[] bootTime = new byte[8];
        ThreadLocalRandom.current().nextBytes(owner);
        Bytes.putLong(bootTime, 0, System.currentTimeMillis());
        return stateHandler.createClient(address, address, minor, owner, new verifier4(bootTime), null, false);
    }

    public static Inode generateFileHandle() {
        byte[] b = new byte[nfs4_prot.NFS4_FHSIZE];
        ThreadLocalRandom.current().nextBytes(b);
        return  Inode.forFile(b);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FileTrackerBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
