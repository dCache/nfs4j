package org.dcache.nfs.benchmarks;

import org.dcache.nfs.util.Cache;
import org.dcache.nfs.util.NopCacheEventListener;
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

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
public class CacheBenchmark {

    @State(Scope.Benchmark)
    public static class CacheHolder {

        private Cache<String, String> cache;

        @Setup
        public void setUp() {
            cache = new Cache<>("test cache", 64, Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    new NopCacheEventListener());
            cache.put("foo", "bar");
        }

        public Cache<String, String> getCache() {
            return cache;
        }
    }

    @Benchmark
    @Threads(16)
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public long cachePutRemoveBenchmark(CacheHolder cacheHolder) {

        final var cache = cacheHolder.getCache();
        var key = "key";
        var val = "val";

        cache.put(key, val);
        cache.remove(key);

        return cache.lastClean();
    }

    @Benchmark
    @Threads(16)
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public String cacheGetBenchmark(CacheHolder cacheHolder) {

        final var cache = cacheHolder.getCache();
        return cache.get("foo");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CacheBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
