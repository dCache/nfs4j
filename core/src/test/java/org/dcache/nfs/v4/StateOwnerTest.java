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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;

import org.dcache.nfs.status.BadSeqidException;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.junit.Before;
import org.junit.Test;

/**
 * Sequence id handling of nfsv4.0 state owners, see rfc7530#section-9.1.7.
 */
public class StateOwnerTest {

    private StateOwner owner;

    @Before
    public void setUp() {
        // owner created by a request carrying sequence id 1, see NFS4Client#getOrCreateOwner
        owner = owner("state owner test");
    }

    @Test
    public void testSequenceInOrderIsAccepted() throws Exception {
        for (int i = 1; i <= 3; i++) {
            seqid4 seqid = new seqid4(i);
            assertFalse("in order sequence treated as replay", owner.isSequenceReplay(seqid));
            owner.consumeSequence(seqid, new nfs_resop4());
        }
    }

    @Test
    public void testRetransmitDetectedAsReplay() throws Exception {
        nfs_resop4 response = new nfs_resop4();
        owner.consumeSequence(new seqid4(1), response);

        assertTrue("retransmission not detected", owner.isSequenceReplay(new seqid4(1)));
        assertSame("cached response of the original request expected", response, owner.replayLastResponse());
    }

    @Test
    public void testRetransmitOfFailureRepliedWithFailure() throws Exception {
        NoEntException failure = new NoEntException();
        owner.consumeSequenceOnFailure(new seqid4(1), failure);

        assertTrue("retransmission not detected", owner.isSequenceReplay(new seqid4(1)));
        try {
            owner.replayLastResponse();
            fail("recorded failure of the original request expected");
        } catch (NoEntException e) {
            assertSame(failure, e);
        }
    }

    @Test(expected = BadSeqidException.class)
    public void testOutOfOrderSequenceIsRejected() throws Exception {
        owner.consumeSequence(new seqid4(1), new nfs_resop4());
        owner.isSequenceReplay(new seqid4(7));
    }

    @Test
    public void testDelayDoesNotConsumeSequence() throws Exception {
        assertFalse(owner.isSequenceReplay(new seqid4(1)));
        owner.consumeSequenceOnFailure(new seqid4(1), new DelayException());

        // the client retries with the same sequence id, which must not be treated as a retransmission
        assertFalse("sequence id consumed by NFS4ERR_DELAY", owner.isSequenceReplay(new seqid4(1)));
        owner.consumeSequence(new seqid4(1), new nfs_resop4());
    }

    @Test
    public void testSequenceOfOwnerIsIndependent() throws Exception {
        final StateOwner other = owner("other state owner");
        owner.consumeSequence(new seqid4(1), new nfs_resop4());
        try {
            assertFalse(other.isSequenceReplay(new seqid4(1)));
        } catch (BadSeqidException e) {
            fail("Sequence id must not be shared between owners");
        }
    }

    private static StateOwner owner(String name) {
        state_owner4 so = new state_owner4();
        so.clientid = new clientid4(1);
        so.owner = name.getBytes(StandardCharsets.UTF_8);
        return StateOwner.expectingSequence(so, new seqid4(1));
    }
}
