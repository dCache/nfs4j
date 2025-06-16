/*
 * Copyright (c) 2009 - 2025 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.nfs.v4;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.SeqMisorderedException;
import org.dcache.nfs.util.ManualClock;
import org.dcache.nfs.util.NopCacheEventListener;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.junit.Before;
import org.junit.Test;

public class NFS4ClientTest {

    private NFSv4StateHandler stateHandler;
    private NFS4Client nfsClient;
    private StateOwner owner;
    private ManualClock clock;

    @Before
    public void setUp() throws UnknownHostException, ChimeraNFSException {

        clock = new ManualClock();
        var leaseTime = Duration.ofSeconds(NFSv4Defaults.NFS4_LEASE_TIME);
        var clientStore = new EphemeralClientRecoveryStore();
        stateHandler = new NFSv4StateHandler(leaseTime, 0, clientStore,
                new DefaultClientCache(leaseTime, new NopCacheEventListener<>()), clock);

        nfsClient = createClient(stateHandler);
        owner = nfsClient.getOrCreateOwner("client test".getBytes(StandardCharsets.UTF_8),
                new seqid4(0));
    }

    @Test
    public void testStateCleanOnOpenCloseV41() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        NFSv41Session session = nfsClient.createSession(1, 2, 1, 8, 8);
        NFS4State state = nfsClient.createOpenState(owner);

        nfs_argop4 close_args = new CompoundBuilder()
                .withClose(state.stateid(), 1)
                .build().argarray[0];

        OperationCLOSE CLOSE = new OperationCLOSE(close_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CLOSE);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withMinorversion(1)
                .withDeviceManager(mock(NFSv41DeviceManager.class))
                .withCall(generateRpcCall())
                .build();

        context.currentInode(Inode.forFile(new byte[] {}));
        context.setSession(session);

        AssertNFS.assertNFS(CLOSE, context, result, nfsstat.NFS_OK);
        assertFalse("client state not cleaned", nfsClient.hasState());
    }

    @Test
    public void testStateCleanOnOpenCloseV40() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        NFS4State state = nfsClient.createOpenState(owner);

        nfs_argop4 close_args = new CompoundBuilder()
                .withClose(state.stateid(), 1)
                .build().argarray[0];

        OperationCLOSE CLOSE = new OperationCLOSE(close_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CLOSE);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withMinorversion(0)
                .withCall(generateRpcCall())
                .build();

        context.currentInode(Inode.forFile(new byte[] {}));

        AssertNFS.assertNFS(CLOSE, context, result, nfsstat.NFS_OK);
        assertFalse("client state not cleaned", nfsClient.hasState());
    }

    @Test
    public void testInitiallyNoStates() throws ChimeraNFSException {
        assertFalse(nfsClient.hasState());
    }

    @Test
    public void testAttacheDetachState() throws ChimeraNFSException {

        state_owner4 so = new state_owner4();
        so.clientid = nfsClient.getId();
        so.owner = "someOtherOwner".getBytes(StandardCharsets.UTF_8);
        StateOwner stateOwner = new StateOwner(so, 0);
        NFS4State state = new NFS4State(stateOwner, new stateid4(new byte[] {}, 0));

        nfsClient.attachState(state);
        assertTrue(nfsClient.hasState());

        nfsClient.detachState(state);
        assertFalse(nfsClient.hasState());
    }

    @Test
    public void testCreateState() throws ChimeraNFSException {
        NFS4State state = nfsClient.createOpenState(owner);
        assertTrue(nfsClient.hasState());
    }

    @Test
    public void testFreshV41ClientNeedReclaim() {
        assertTrue(nfsClient.needReclaim());
    }

    @Test
    public void testFreshV40ClientDontNeedReclaim() throws UnknownHostException {
        assertFalse(createClient(stateHandler, 0).needReclaim());
    }

    @Test
    public void testCreateSessionReply() throws ChimeraNFSException {

        NFSv41Session session1 = nfsClient.createSession(1, 0, 0, 0, 1);
        NFSv41Session session2 = nfsClient.createSession(1, 0, 0, 0, 1);
        assertEquals(session1.id(), session2.id());
    }

    @Test(expected = SeqMisorderedException.class)
    public void testCreateSessionWrongSequence() throws ChimeraNFSException {
        nfsClient.createSession(2, 0, 0, 0, 1);
    }

    @Test
    public void testClientDisposeCleansState() throws ChimeraNFSException {
        AtomicBoolean isDisposed = new AtomicBoolean(false);

        NFS4State state = nfsClient.createOpenState(owner);
        state.addDisposeListener(s -> isDisposed.set(true));

        nfsClient.tryDispose();
        assertTrue("client state is not disposed", isDisposed.get());
        assertFalse("client claims to have a state after dispose", nfsClient.hasState());
    }

    @Test
    public void testClientValidityBeforeLeaseExpired() throws ChimeraNFSException {

        assertTrue(nfsClient.isLeaseValid());

        clock.advance(stateHandler.getLeaseTime().minus(1, ChronoUnit.SECONDS));
        assertTrue("Client should be valid before lease have expired.", nfsClient.isLeaseValid());
    }

    @Test
    public void testClientValidityAfterLeaseExpired() throws ChimeraNFSException {

        assertTrue(nfsClient.isLeaseValid());

        clock.advance(stateHandler.getLeaseTime().plus(1, ChronoUnit.SECONDS));
        assertFalse("Client can be valid with expired lease", nfsClient.isLeaseValid());
    }
}
