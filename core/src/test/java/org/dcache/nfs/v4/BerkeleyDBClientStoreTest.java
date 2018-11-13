package org.dcache.nfs.v4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;

import org.dcache.nfs.status.ReclaimBadException;
import org.dcache.nfs.status.NoGraceException;

import static org.junit.Assert.*;
import static java.nio.charset.StandardCharsets.UTF_8;
/**
 *
 */
public class BerkeleyDBClientStoreTest {

    private BerkeleyDBClientStore clientRecoveryStore;
    private Path storeDirectory;

    @Before
    public void setUp() throws IOException {
        storeDirectory = Files.createTempDirectory("nfs-client-store");
    }

    @After
    public void tearDown() throws Exception {
        clientRecoveryStore.close();
        Files.list(storeDirectory).forEach(f -> {
            try {
            Files.delete(f);
            } catch (IOException e) {
                // as we fail to delete, directory remove will fail as well
            }
        });
        Files.delete(storeDirectory);
    }

    @Test
    public void shouldNotWaitForClientsOnFirstStart() {
        givenServer();
        assertFalse(clientRecoveryStore.waitingForReclaim());
    }

    @Test
    public void shouldCloseReclaimWindowOnComplete() throws Exception {
        givenServer();
        clientRecoveryStore.reclaimComplete();
        assertFalse(clientRecoveryStore.waitingForReclaim());
    }

    @Test
    public void shouldWaitForClientsAfterRestart() throws Exception {
        givenServer();
        clientRecoveryStore.addClient("client1".getBytes(UTF_8));
        reboot();

        assertTrue(clientRecoveryStore.waitingForReclaim());
    }

    @Test(expected = ReclaimBadException.class)
    public void shouldFailWhenNewClientWantReclaim() throws Exception {
        givenServer();
        clientRecoveryStore.addClient("client1".getBytes(UTF_8));
        clientRecoveryStore.wantReclaim("client1".getBytes(UTF_8));
    }

    @Test
    public void shouldReclaimAfterReboot() throws Exception {
        givenServer();
        clientRecoveryStore.addClient("client1".getBytes(UTF_8));
        reboot();
        clientRecoveryStore.addClient("client1".getBytes(UTF_8));
        clientRecoveryStore.wantReclaim("client1".getBytes(UTF_8));
    }

    @Test(expected = ReclaimBadException.class)
    public void shouldFailReclaimAfterRemove() throws Exception {
        givenServer();
        clientRecoveryStore.addClient("client1".getBytes(UTF_8));
        clientRecoveryStore.removeClient("client1".getBytes(UTF_8));
        clientRecoveryStore.wantReclaim("client1".getBytes(UTF_8));
    }

    @Test(expected = NoGraceException.class)
    public void shouldFailOnLateReclaim() throws Exception {
        givenServer();
        clientRecoveryStore.reclaimComplete();
        clientRecoveryStore.wantReclaim("client1".getBytes(UTF_8));
    }

    private void givenServer() {
        clientRecoveryStore = new BerkeleyDBClientStore(storeDirectory.toFile());
    }

    private void reboot() throws Exception {
        clientRecoveryStore.close();
        clientRecoveryStore = new BerkeleyDBClientStore(storeDirectory.toFile());
    }
}
