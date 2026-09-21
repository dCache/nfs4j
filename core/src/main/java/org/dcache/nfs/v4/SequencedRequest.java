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

import java.io.IOException;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.oncrpc4j.rpc.OncRpcException;

/**
 * Executes a sequenced nfsv4.0 request under the rules of rfc7530#section-9.1.7: a retransmission of the last
 * request is answered with the cached response of the original request without executing the operation again, and
 * the sequence id is consumed once the operation completed, unless it failed with an error that does not consume a
 * sequence id. For minor versions with sessions the operation is executed as is, as the sequence ids of the
 * operation arguments are obsolete.
 */
class SequencedRequest {

    interface Body {
        void execute() throws IOException, OncRpcException;
    }

    private SequencedRequest() {
    }

    static void process(CompoundContext context, StateOwner owner, seqid4 seqid, nfs_resop4 result, Body body)
            throws IOException, OncRpcException {

        if (context.getMinorversion() != 0) {
            body.execute();
            return;
        }

        if (owner.isSequenceReplay(seqid)) {
            replay(owner.replayLastResponse(), result);
            return;
        }

        try {
            body.execute();
            owner.consumeSequence(seqid, result);
        } catch (ChimeraNFSException e) {
            owner.consumeSequenceOnFailure(seqid, e);
            throw e;
        } catch (IOException | RuntimeException e) {
            owner.consumeSequenceOnFailure(seqid, new ServerFaultException(e.getMessage(), e));
            throw e;
        }
    }

    private static void replay(nfs_resop4 cached, nfs_resop4 result) {
        result.opopen = cached.opopen;
        result.opclose = cached.opclose;
        result.opopen_confirm = cached.opopen_confirm;
        result.opopen_downgrade = cached.opopen_downgrade;
        result.oplock = cached.oplock;
        result.oplocku = cached.oplocku;
    }
}
