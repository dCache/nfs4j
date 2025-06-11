/*
 * Copyright (c) 2017 - 2025 Deutsches Elektronen-Synchroton,
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

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.ShareDeniedException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.vfs.Inode;

import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_WANT_READ_DELEG;
import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.dcache.nfs.v4.NfsTestUtils.generateFileHandle;

import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_WRITE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_BOTH;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_WANT_NO_DELEG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.dcache.nfs.v4.xdr.seqid4;

public class FileTrackerTest {

    private FileTracker tracker;
    private NFSv4StateHandler sh;

    @Before
    public void setUp() {
        sh = new NFSv4StateHandler();
        tracker = new FileTracker();
    }

    @Test
    public void shouldAllowNonConflictingOpens() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, 0);
    }

    @Test
    public void shouldReturnSameStateIdForSameClient() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        var openRecord2 = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, 0);
        assertEquals("New stateid returned", openRecord1.openStateId(), openRecord2.openStateId());
    }

    @Test
    public void shouldReturnDifferentStateIdForDifferentOwners() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        StateOwner stateOwner2 = client1.getOrCreateOwner("client2".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        var openRecord2 = tracker.addOpen(client1, stateOwner2, inode, OPEN4_SHARE_ACCESS_READ, 0);
        assertNotEquals("Same stateid for different owners returned", openRecord1.openStateId(), openRecord2.openStateId());
    }

    @Test
    public void shouldMergeAccessModesOnMultipleOpenes() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        var openRecord = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, 0);
        int accessMode = tracker.getShareAccess(client1, inode, openRecord.openStateId());
        assertEquals("Access mode not merged", OPEN4_SHARE_ACCESS_BOTH, accessMode);
    }

    @Test
    public void shouldChangeAccessModesAfterDowngrade() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        var openRecord = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        tracker.downgradeOpen(client1, openRecord.openStateId(), inode, OPEN4_SHARE_ACCESS_READ, 0);

        int accessMode = tracker.getShareAccess(client1, inode, openRecord.openStateId());
        assertEquals("Access mode not changed on downgrade", OPEN4_SHARE_ACCESS_READ, accessMode);
    }

    @Test(expected = InvalException.class)
    public void shouldRejectDowngradeToNotOwnedMode() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        var openRecord = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);

        tracker.downgradeOpen(client1, openRecord.openStateId(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);
    }

    @Test(expected = InvalException.class)
    public void shouldRejectDowngradeDenyToNotOwnedMode() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_BOTH, OPEN4_SHARE_ACCESS_READ);
        var openRecord = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        tracker.downgradeOpen(client1, openRecord.openStateId(), inode, OPEN4_SHARE_ACCESS_READ, OPEN4_SHARE_ACCESS_WRITE);
    }

    @Test
    public void shouldReturnDifferentStateIdForDifferentClient() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        NFS4Client client2 = createClient(sh);
        StateOwner stateOwner2 = client1.getOrCreateOwner("client2".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        var openRecord2 = tracker.addOpen(client2, stateOwner2, inode, OPEN4_SHARE_ACCESS_WRITE, 0);
        assertNotEquals("Same stateid returned", openRecord1.openStateId(), openRecord2.openStateId());
    }

    @Test(expected = ShareDeniedException.class)
    public void shouldRejectConflictingOpens() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, OPEN4_SHARE_ACCESS_READ);
    }

    @Test
    public void shouldAllowConflictingOpensAfterRemove() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.removeOpen(inode, openRecord.openStateId());

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_WRITE, OPEN4_SHARE_ACCESS_READ);
    }

    @Test(expected = BadStateidException.class)
    public void shouldFailToGetAccessModeWithBadStateid() throws Exception {

        NFS4Client client1 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.getShareAccess(client1, inode, client1.createOpenState(stateOwner1).stateid());
    }

    @Test
    public void shouldGetReadDelegation() throws Exception {

        NFS4Client client = createClient(sh);
        ClientCB mockCallBack = mock(ClientCB.class);
        client.setCB(mockCallBack);

        StateOwner stateOwner1 = client.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord = tracker.addOpen(client, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_READ_DELEG, 0);
        assertTrue("Read delegation not granted", openRecord.hasDelegation());
    }

    @Test
    public void shouldNotReadDelegation() throws Exception {

        NFS4Client client = createClient(sh);
        ClientCB mockCallBack = mock(ClientCB.class);
        client.setCB(mockCallBack);

        StateOwner stateOwner1 = client.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord = tracker.addOpen(client, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        assertFalse("Read delegation is granted, but not requested", openRecord.hasDelegation());
    }

    @Test
    public void shouldReCallReadDelegationOnConflict() throws Exception {

        NFS4Client client1 = createClient(sh);
        NFS4Client client2 = createClient(sh);

        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        StateOwner stateOwner2 = client2.getOrCreateOwner("client2".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_READ_DELEG, 0);
        try {
            var openRecord2 = tracker.addOpen(client2, stateOwner2, inode, OPEN4_SHARE_ACCESS_WRITE,
                  0);
            fail("Delay exception expected");
        } catch (DelayException e) {
            // expected
        }

        verify(client1.getCB()).cbDelegationRecall(any(), any(), anyBoolean());
    }

    @Test
    public void shouldAllowMultipleReadDelegation() throws Exception {

        NFS4Client client1 = createClient(sh);
        NFS4Client client2 = createClient(sh);

        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        StateOwner stateOwner2 = client2.getOrCreateOwner("client2".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_READ_DELEG, 0);
        var openRecord2 = tracker.addOpen(client2, stateOwner2, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_READ_DELEG, 0);

        assertTrue("Read delegation not granted", openRecord2.hasDelegation());

    }

    @Test
    public void shouldIssueReadDelegationOnMultipleOpens() throws Exception {

        NFS4Client client = createClient(sh);
        StateOwner stateOwner = client.getOrCreateOwner("client".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ, 0);
        assertFalse("Delegation not expected, but granted", openRecord1.hasDelegation());

        var openRecord2 = tracker.addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ, 0);
        assertTrue("Read opportunistic delegation not granted", openRecord2.hasDelegation());
    }

    @Test
    public void shouldNotIssueReadDelegation() throws Exception {

        NFS4Client client = createClient(sh);
        StateOwner stateOwner = client.getOrCreateOwner("client".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        var openRecord1 = tracker.addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_NO_DELEG, 0);
        assertFalse("Unwanted delegation", openRecord1.hasDelegation());

        var openRecord2 = tracker.addOpen(client, stateOwner, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_NO_DELEG, 0);
        assertFalse("Unwanted delegation", openRecord2.hasDelegation());
    }


    @Test
    public void getOpenFiles() throws UnknownHostException, ChimeraNFSException {

        NFS4Client client1 = createClient(sh);
        NFS4Client client2 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        StateOwner stateOwner2 = client2.getOrCreateOwner("client2".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.addOpen(client2, stateOwner2, inode, OPEN4_SHARE_ACCESS_READ, 0);

        var openFiles = tracker.getOpenFiles();

        assertEquals("Number of open files not as expected", 1, openFiles.size());

        var clients = openFiles.get(inode);
        assertThat("Expected clients not found", clients, Matchers.containsInAnyOrder(client1,client2));
    }

    @Test
    public void getDelegations() throws UnknownHostException, ChimeraNFSException {

        NFS4Client client1 = createClient(sh);
        NFS4Client client2 = createClient(sh);
        StateOwner stateOwner1 = client1.getOrCreateOwner("client1".getBytes(StandardCharsets.UTF_8), new seqid4(0));
        StateOwner stateOwner2 = client2.getOrCreateOwner("client2".getBytes(StandardCharsets.UTF_8), new seqid4(0));

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, stateOwner1, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_READ_DELEG, 0);
        tracker.addOpen(client2, stateOwner2, inode, OPEN4_SHARE_ACCESS_READ | OPEN4_SHARE_ACCESS_WANT_READ_DELEG, 0);

        var delegations = tracker.getDelegations();

        assertEquals("Number of open files not as expected", 1, delegations.size());

        var clients = delegations.get(inode);
        assertThat("Expected clients not found", clients, Matchers.containsInAnyOrder(client1,client2));
    }
}
