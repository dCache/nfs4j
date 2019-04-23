package org.dcache.nfs.benchmarks;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.dcache.nfs.InetAddressMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 *
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
public class IpMatcherBenchmark {

    private InetAddressMatcher addressMatcher;
    private InetAddress address;

    @Param({"192.168.1.1", "192.168.1.0/24", "fe80::9cef:10f5:f2ae:1aa1", "fe80::9cef:10f5:f2ae:1aa1/48"})
    private String template;

    @Param({"192.168.1.1", "192.168.5.1", "fe80::9cef:10f5:f2ae:1aa1", "fe80:cd00:0:cde:1257:0:211e:729c"})
    private String client;

    @Setup
    public void setUp() throws UnknownHostException {
        addressMatcher = InetAddressMatcher.forPattern(template);
        address = InetAddresses.forString(client);
    }

    @Benchmark
    public boolean benchmark() {
        return addressMatcher.match(address);
    }

}
