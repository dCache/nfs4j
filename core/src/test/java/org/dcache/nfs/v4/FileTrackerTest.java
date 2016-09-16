/*
 * Copyright (c) 2016 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.ShareDeniedException;
import org.junit.Before;
import org.junit.Test;

import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.v4.xdr.stateid4;

import static org.junit.Assert.*;
import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.dcache.nfs.v4.NfsTestUtils.generateFileHandle;

import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_WRITE;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_BOTH;

public class FileTrackerTest {

    FileTracker tracker;
    NFSv4StateHandler sh;

    @Before
    public void setUp() {
        sh = new NFSv4StateHandler();
        tracker = new FileTracker();
    }

    @Test
    public void shouldAllowNonConflictingOpens() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);
    }

    @Test
    public void shouldReturnSameStateIdForSameClient() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        stateid4 s1 = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        stateid4 s2 = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);
        assertEquals("New stateid returned", s1, s2);
    }

    @Test
    public void shouldMergeAccessModesOnMultipleOpenes() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        stateid4 s = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);
        int accessMode = tracker.getShareAccess(client1, inode, s);
        assertEquals("Access mode not merged", OPEN4_SHARE_ACCESS_BOTH, accessMode);
    }

    @Test
    public void shouldChangeAccessModesAfterDowngrade() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        stateid4 s = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        tracker.downgradeOpen(client1, s, inode, OPEN4_SHARE_ACCESS_READ, 0);

        int accessMode = tracker.getShareAccess(client1, inode, s);
        assertEquals("Access mode not changed on downgrade", OPEN4_SHARE_ACCESS_READ, accessMode);
    }

    @Test(expected = InvalException.class)
    public void shouldRejectDowngradeToNotOwnedMode() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        stateid4 s = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);

        tracker.downgradeOpen(client1, s, inode, OPEN4_SHARE_ACCESS_WRITE, 0);
    }

    @Test(expected = InvalException.class)
    public void shouldRejectDowngradeDenyToNotOwnedMode() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_BOTH, OPEN4_SHARE_ACCESS_READ);
        stateid4 s = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);

        tracker.downgradeOpen(client1, s, inode, OPEN4_SHARE_ACCESS_READ, OPEN4_SHARE_ACCESS_WRITE);
    }

    @Test
    public void shouldReturnDifferentStateIdForDifferentClient() throws Exception {

        NFS4Client client1 = createClient(sh);
        NFS4Client client2 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        stateid4 s1 = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        stateid4 s2 = tracker.addOpen(client2, client2.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, 0);
        assertNotEquals("Same stateid returned", s1, s2);
    }

    @Test(expected = ShareDeniedException.class)
    public void shouldRejectConflictingOpens() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, OPEN4_SHARE_ACCESS_READ);
    }

    @Test
    public void shouldAllowConflictingOpensAfterRemove() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        stateid4 s = tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.removeOpen(inode, s);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_WRITE, OPEN4_SHARE_ACCESS_READ);
    }

    @Test(expected = BadStateidException.class)
    public void shouldFailToGetAccessModeWithBadStateid() throws Exception {

        NFS4Client client1 = createClient(sh);

        nfs_fh4 fh = generateFileHandle();
        Inode inode = Inode.forFile(fh.value);

        tracker.addOpen(client1, client1.asStateOwner(), inode, OPEN4_SHARE_ACCESS_READ, 0);
        tracker.getShareAccess(client1, inode, client1.createState(client1.asStateOwner()).stateid());
    }

}
