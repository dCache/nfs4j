package org.dcache.nfs.v4.nlm;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class DistributedLockManagerTest {

    private HazelcastInstance hzSerrver;
    private HazelcastInstance hzClient;
    private LockManager lm1;
    private LockManager lm2;
    private byte[] file1;

    @Before
    public void setUp() throws Exception {

        file1 = "file1".getBytes(StandardCharsets.UTF_8);

        hzSerrver = Hazelcast.newHazelcastInstance();

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().setAddresses(Arrays.asList(

                hzSerrver.getCluster().getLocalMember().getAddress().getHost() +
                        ":" + hzSerrver.getCluster().getLocalMember().getAddress().getPort()));

        hzClient = HazelcastClient.newHazelcastClient(config);
        lm1 = new DistributedLockManager(hzClient, "distributed-byte-range-lock");
        lm2 = new DistributedLockManager(hzClient, "distributed-byte-range-lock");
    }


    @Test(expected = LockDeniedException.class)
    public void testConflictingLockDifferentOwner() throws LockException {
        NlmLock lock1 = new SimpleLmTest.LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        lm1.lock(file1, lock1);

        NlmLock lock2 = new SimpleLmTest.LockBuilder()
                .withOwner("owner2")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        lm2.lock(file1, lock2);
    }

    @Test
    public void testConflictingLockSameOwner() throws LockException {
        NlmLock lock1 = new SimpleLmTest.LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forRead()
                .build();
        lm1.lock(file1, lock1);

        NlmLock lock2 = new SimpleLmTest.LockBuilder()
                .withOwner("owner1")
                .from(0)
                .length(1)
                .forWrite()
                .build();
        lm2.lock(file1, lock2);

        try {
            NlmLock lock3 = new SimpleLmTest.LockBuilder()
                    .withOwner("owner2")
                    .from(0)
                    .length(1)
                    .forWrite()
                    .build();

            lm1.test(file1, lock3);
            fail("Lock on lm1 is lost");
        } catch (LockDeniedException e) {
            // pass
        }
    }

    @After
    public void tearDown() {
        if (hzClient != null) {
            hzClient.shutdown();
        }
        if (hzSerrver != null) {
            hzSerrver.shutdown();
        }
    }
}
