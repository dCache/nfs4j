package org.dcache.nfs.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.dcache.nfs.v4.NFS4Client;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.vfs.Inode;
import org.junit.Before;
import org.junit.Test;

public class AdaptiveDelegationLogicTest {

    public static final int EVICTION_QUEUE_CAPACITY = 53;
    public static final int ACTIVE_QUEUE_CAPACITY = 17;

    private AdaptiveDelegationLogic logic;
    private ManualClock clock;
    private NFS4Client client1;
    private NFS4Client client2;

    @Before
    public void setUp() {
        client1 = mock(NFS4Client.class);
        when(client1.getId()).thenReturn(new clientid4(1L));
        client2 = mock(NFS4Client.class);
        when(client2.getId()).thenReturn(new clientid4(2L));

        clock = new ManualClock();
        logic = new AdaptiveDelegationLogic(ACTIVE_QUEUE_CAPACITY, EVICTION_QUEUE_CAPACITY, Duration.ofSeconds(10),
                clock);
    }

    @Test
    public void shouldDelegateReturnsFalseForFirstTime() {
        Inode inode = Inode.forFile("file1".getBytes());
        assertFalse("File in eviction queue should not be delegated", logic.shouldDelegate(client1, inode));
    }

    @Test
    public void shouldDelegateReturnsTrueForActiveQueueFile() {
        Inode inode = Inode.forFile("file1".getBytes());
        logic.shouldDelegate(client1, inode); // Add to eviction queue
        assertTrue("File in active queue should be delegated", logic.shouldDelegate(client1, inode));
    }

    @Test
    public void isActiveReturnsTrueForActiveQueueFile() {
        Inode inode = Inode.forFile("file4".getBytes());
        logic.shouldDelegate(client1, inode); // Add to eviction queue
        boolean delegate = logic.shouldDelegate(client1, inode); // Move to active queue, delegate
        assertTrue("File in active queue should be active", logic.isInActive(client1, inode));
        assertTrue("File in active queue suggest delegation", delegate);
    }

    @Test
    public void isActiveReturnsFalseForEvictionQueueFile() {
        Inode inode = Inode.forFile("file5".getBytes());
        logic.shouldDelegate(client1, inode); // Add to eviction queue
        assertFalse("File in eviction queue should not be active", logic.isInActive(client1, inode));
    }

    @Test
    public void isInEvictionQueueReturnsTrueForEvictionQueueFile() {
        Inode inode = Inode.forFile("file6".getBytes());
        logic.shouldDelegate(client1, inode); // Add to eviction queue
        assertTrue("File in eviction queue should be detected", logic.isInEvictionQueue(client1, inode));
    }

    @Test
    public void isInEvictionQueueReturnsFalseForActiveQueueFile() {
        Inode inode = Inode.forFile("file7".getBytes());
        logic.shouldDelegate(client1, inode); // Add to eviction queue
        logic.shouldDelegate(client1, inode); // Move to active queue, delegate
        assertFalse("File in active queue should not be in eviction queue", logic.isInEvictionQueue(client1, inode));
    }

    @Test
    public void activeQueueEvictsLeastRecentlyUsedFileToEvictionQueue() {
        Inode inode1 = Inode.forFile("file".getBytes());

        logic.shouldDelegate(client1, inode1); // Add to eviction queue

        // trigger eviction from active queue
        for (int i = 0; i < ACTIVE_QUEUE_CAPACITY; i++) {
            // call twice to trigger move to active queue
            logic.shouldDelegate(client1, Inode.forFile(("file" + (i + 10)).getBytes())); // Fill eviction queue
            logic.shouldDelegate(client1, Inode.forFile(("file" + (i + 10)).getBytes())); // Fill active queue
        }

        assertTrue("File should be in eviction queue", logic.isInEvictionQueue(client1, inode1));
    }

    @Test
    public void shouldDiscardExpiredEntries() {
        Inode inode1 = Inode.forFile("file".getBytes());

        logic.shouldDelegate(client1, inode1); // Add to eviction queue
        logic.shouldDelegate(client1, inode1); // Add to active queue

        clock.advance(Duration.ofSeconds(11));

        // trigger eviction from active queue
        for (int i = 0; i < ACTIVE_QUEUE_CAPACITY; i++) {
            // call twice to trigger move to active queue
            logic.shouldDelegate(client1, Inode.forFile(("file" + (i + 10)).getBytes())); // Fill eviction queue
            logic.shouldDelegate(client1, Inode.forFile(("file" + (i + 10)).getBytes())); // Fill active queue
        }

        assertFalse("Expired entry should not be in active queue", logic.isInActive(client1, inode1));
        assertFalse("Expired entry should not be in eviction queue", logic.isInEvictionQueue(client1, inode1));

    }

    @Test
    public void shouldMoveToEvictionQueueIfIdle() {
        Inode inode1 = Inode.forFile("file".getBytes());

        logic.shouldDelegate(client1, inode1); // Add to eviction queue
        logic.shouldDelegate(client1, inode1); // Add to active queue

        clock.advance(Duration.ofSeconds(11));

        logic.shouldDelegate(client1, inode1); // Move to eviction queue as being idle

        assertTrue("File should be in eviction queue", logic.isInEvictionQueue(client1, inode1));
    }

    @Test
    public void differentClietnsShouldNotAffectEachother() {
        Inode inode1 = Inode.forFile("file".getBytes());

        logic.shouldDelegate(client1, inode1);
        logic.shouldDelegate(client2, inode1);

        assertFalse("File should be in eviction queue", logic.isInActive(client1, inode1));
        assertFalse("File should be in eviction queue", logic.isInActive(client1, inode1));
    }

    @Test
    public void clearRemovesAllEntriesFromQueues() {
        Inode inode = Inode.forFile("file14".getBytes());
        logic.shouldDelegate(client1, inode); // Add to eviction queue
        logic.reset();
        assertFalse("Active queue should be empty after clear", logic.isInActive(client1, inode));
        assertFalse("Eviction queue should be empty after clear", logic.isInEvictionQueue(client1, inode));
    }

}
