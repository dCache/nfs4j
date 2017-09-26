/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.v4.client.CompoundBuilder;
import org.dcache.nfs.v4.xdr.sequenceid4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.sessionid4;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.state_protect_how4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import java.util.UUID;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadSessionException;
import org.dcache.nfs.status.ConnNotBoundToSessionException;
import org.junit.Before;
import org.junit.Test;

import static org.dcache.nfs.v4.NfsTestUtils.*;

public class OperationCREATE_SESSIONTest {

    private NFSv4StateHandler stateHandler;
    private final String domain = "nairi.desy.de";
    private final String name = "dCache.ORG java based client";
    private String clientId;

    @Before
    public void setUp() {
        stateHandler = new NFSv4StateHandler();
        clientId = UUID.randomUUID().toString();
    }

    @Test
    public void testCreateSession() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = new CompoundBuilder()
                .withExchangeId(domain, name, clientId, 0, state_protect_how4.SP4_NONE)
                .build().argarray[0];

        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);

        nfs_argop4 cretaesession_args = new CompoundBuilder()
                .withCreatesession(
                        result.opexchange_id.eir_resok4.eir_clientid,
                        result.opexchange_id.eir_resok4.eir_sequenceid)
                .build().argarray[0];

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CREATE_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

         AssertNFS.assertNFS(CREATE_SESSION, context, result, nfsstat.NFS_OK);
    }

    @Test
    public void testCreateSessionNoClient() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 cretaesession_args = new CompoundBuilder()
                .withCreatesession(new clientid4(0), new sequenceid4(0))
                .build().argarray[0];

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CREATE_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

       AssertNFS.assertNFS(CREATE_SESSION, context, result, nfsstat.NFSERR_STALE_CLIENTID);
    }

    @Test
    public void testCreateSessionMisordered() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = new CompoundBuilder()
                .withExchangeId(domain, name, clientId, 0, state_protect_how4.SP4_NONE)
                .build().argarray[0];

        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);

        sequenceid4 badSequence = new sequenceid4(result.opexchange_id.eir_resok4.eir_sequenceid.value + 1);
        nfs_argop4 cretaesession_args = new CompoundBuilder()
                .withCreatesession(
                        result.opexchange_id.eir_resok4.eir_clientid,
                        badSequence)
                .build().argarray[0];

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CREATE_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        AssertNFS.assertNFS(CREATE_SESSION, context, result, nfsstat.NFSERR_SEQ_MISORDERED);
    }

    @Test(expected = BadSessionException.class)
    public void testDestroySession() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = new CompoundBuilder()
                .withExchangeId(domain, name, clientId, 0, state_protect_how4.SP4_NONE)
                .build().argarray[0];

        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);

        nfs_argop4 cretaesession_args = new CompoundBuilder()
                .withCreatesession(
                        result.opexchange_id.eir_resok4.eir_clientid,
                        result.opexchange_id.eir_resok4.eir_sequenceid)
                .build().argarray[0];

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CREATE_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        AssertNFS.assertNFS(CREATE_SESSION, context, result, nfsstat.NFS_OK);

        sessionid4 session = result.opcreate_session.csr_resok4.csr_sessionid;
        nfs_argop4 destroysession_args = new CompoundBuilder()
                .withDestroysession(result.opcreate_session.csr_resok4.csr_sessionid)
                .build().argarray[0];

        OperationDESTROY_SESSION DESTROY_SESSION = new OperationDESTROY_SESSION(destroysession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_DESTROY_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall())
                .build();

        AssertNFS.assertNFS(DESTROY_SESSION, context, result, nfsstat.NFS_OK);
        stateHandler.getClient(session).getSession(session);
    }

    @Test(expected = ConnNotBoundToSessionException.class)
    public void testDestroySessionNoBind() throws Exception {
        CompoundContextBuilder contextBdr = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withCall(generateRpcCall());

        nfs_resop4 result;

        nfs_argop4 exchangeid_args = new CompoundBuilder()
                .withExchangeId(domain, name, clientId, 0, state_protect_how4.SP4_NONE)
                .build().argarray[0];

        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = execute(contextBdr.build(), EXCHANGE_ID);

        nfs_argop4 cretaesession_args = new CompoundBuilder()
                .withCreatesession(
                        result.opexchange_id.eir_resok4.eir_clientid,
                        result.opexchange_id.eir_resok4.eir_sequenceid)
                .build().argarray[0];

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = execute(contextBdr.build(), CREATE_SESSION);
        sessionid4 session = result.opcreate_session.csr_resok4.csr_sessionid;

        nfs_argop4 sequence_args = new CompoundBuilder()
                .withSequence(
                        false,
                        session,
                        0,
                        2,
                        2).build().argarray[0];

        // sequnce implicitly binds connection to session
        OperationSEQUENCE SEQUENCE = new OperationSEQUENCE(sequence_args);
        result = execute(contextBdr.build(), SEQUENCE);

        // new context, new client
        contextBdr = new CompoundContextBuilder()
                .withCall(generateRpcCall())
                .withStateHandler(stateHandler);

        nfs_argop4 destroysession_args = new CompoundBuilder()
                .withDestroysession(session)
                .build().argarray[0];

        OperationDESTROY_SESSION DESTROY_SESSION = new OperationDESTROY_SESSION(destroysession_args);
        result = execute(contextBdr.build(), DESTROY_SESSION);

    }
}
