/*
 * Copyright (c) 2026 Deutsches Elektronen-Synchroton,
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.nio.charset.StandardCharsets;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadSeqidException;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.junit.Before;
import org.junit.Test;

public class StateOwnerTest {

    private StateOwner owner;

    @Before
    public void setUp() {
        state_owner4 so = new state_owner4();
        so.clientid = new clientid4(1L);
        so.owner = "owner".getBytes(StandardCharsets.UTF_8);
        // last consumed seqid is 1
        owner = new StateOwner(so, 1);
    }

    @Test
    public void testNextSequenceExecuted() throws ChimeraNFSException {
        assertNull(owner.acquire(new seqid4(2)));
    }

    @Test(expected = BadSeqidException.class)
    public void testOutOfOrderSequence() throws ChimeraNFSException {
        owner.acquire(new seqid4(3));
    }

    @Test(expected = BadSeqidException.class)
    public void testRetransmitWithoutCachedReply() throws ChimeraNFSException {
        owner.acquire(new seqid4(1));
    }

    @Test
    public void testRetransmitReplayed() throws ChimeraNFSException {
        StateOwner.Reply reply = execute(2, nfsstat.NFS_OK);
        assertSame(reply, owner.acquire(new seqid4(2)));
    }

    @Test
    public void testRetransmitOfFailureReplayed() throws ChimeraNFSException {
        StateOwner.Reply reply = execute(2, nfsstat.NFSERR_NOENT);
        assertSame(reply, owner.acquire(new seqid4(2)));
    }

    @Test
    public void testNonMutatingErrorDoesNotConsumeSequence() throws ChimeraNFSException {
        execute(2, nfsstat.NFSERR_BAD_STATEID);
        assertNull(owner.acquire(new seqid4(2)));
    }

    @Test
    public void testDelayConsumesSequence() throws ChimeraNFSException {
        execute(2, nfsstat.NFSERR_DELAY);
        // client advancing the seqid as required by rfc7530#section-9.1.7
        assertNull(owner.acquire(new seqid4(3)));
    }

    @Test
    public void testRetryOfDelayedRequestExecuted() throws ChimeraNFSException {
        execute(2, nfsstat.NFSERR_DELAY);
        // client retrying with the same seqid
        assertNull(owner.acquire(new seqid4(2)));
    }

    @Test(expected = DelayException.class)
    public void testDuplicateInProgress() throws ChimeraNFSException {
        owner.acquire(new seqid4(2));
        owner.acquire(new seqid4(2));
    }

    @Test
    public void testDuplicateReplayedOnceComplete() throws ChimeraNFSException {
        owner.acquire(new seqid4(2));
        StateOwner.Reply reply = reply(nfsstat.NFS_OK);
        owner.update(new seqid4(2), reply);
        assertSame(reply, owner.acquire(new seqid4(2)));
    }

    @Test
    public void testReleaseDoesNotConsumeSequence() throws ChimeraNFSException {
        owner.acquire(new seqid4(2));
        owner.release();
        assertNull(owner.acquire(new seqid4(2)));
    }

    private StateOwner.Reply execute(int seqid, int status) throws ChimeraNFSException {
        assertNull(owner.acquire(new seqid4(seqid)));
        StateOwner.Reply reply = reply(status);
        owner.update(new seqid4(seqid), reply);
        return reply;
    }

    private static StateOwner.Reply reply(int status) {
        nfs_resop4 result = nfs_resop4.resopFor(nfs_opnum4.OP_OPEN);
        result.setStatus(status);
        return new StateOwner.Reply(result, null, null);
    }
}
