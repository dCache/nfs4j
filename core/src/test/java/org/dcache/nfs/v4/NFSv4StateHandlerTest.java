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

import org.dcache.nfs.v4.xdr.stateid4;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadSeqidException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.StaleClientidException;
import org.dcache.nfs.v4.xdr.seqid4;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import org.dcache.nfs.v4.xdr.clientid4;

public class NFSv4StateHandlerTest {

    private NFSv4StateHandler _stateHandler;
    private NFS4Client _client;
    private StateOwner _owner;

    @Before
    public void setUp() throws UnknownHostException, BadSeqidException {
        _stateHandler = new NFSv4StateHandler();
        _client = createClient(_stateHandler);
        _owner =  _client.getOrCreateOwner("client test".getBytes(StandardCharsets.UTF_8), new seqid4(0));
    }

    @Test
    public void testGetByStateId() throws Exception {
        stateid4 state = _client.createState(_owner).stateid();
        _stateHandler.getClientIdByStateId(state);
    }

    @Test(expected=StaleClientidException.class)
    public void testGetClientNotExists() throws Exception {
        _stateHandler.getClient(new clientid4(1L));
    }

    @Test
    public void testGetClientExists() throws Exception {
         _client = createClient(_stateHandler);
        assertEquals(_client,  _stateHandler.getClient(_client.getId()));
    }

    @Test
    public void testUpdateLeaseTime() throws Exception {
        NFS4State state = _client.createState(_owner);
        stateid4 stateid = state.stateid();
        state.confirm();
        _stateHandler.updateClientLeaseTime(stateid);
    }

    @Test(expected=BadStateidException.class)
    public void testUpdateLeaseTimeNotConfirmed() throws Exception {
        NFS4State state = _client.createState(_owner);
        stateid4 stateid = state.stateid();

        _stateHandler.updateClientLeaseTime(stateid);
    }

    @Test(expected=BadStateidException.class)
    public void testUpdateLeaseTimeNotExists() throws Exception {
        stateid4 state = _client.createState(_owner).stateid();
        _stateHandler.updateClientLeaseTime(state);
    }

    @Test(expected = IllegalStateException.class)
    public void testUseAfterShutdown() throws Exception {
        NFS4State state = _client.createState(_owner);
        stateid4 stateid = state.stateid();
        state.confirm();
        _stateHandler.shutdown();
        _stateHandler.updateClientLeaseTime(stateid);
    }

    @Test
    public void testInstanceId() {
        int instanceId = 18;
        NFSv4StateHandler stateHandler = new NFSv4StateHandler(2, instanceId);
        assertEquals("Invalid instance id returned", instanceId, stateHandler.getInstanceId());
    }

    @Test
    public void testInstanceIdByStateid() throws UnknownHostException, ChimeraNFSException {
        int instanceId = 117;
        NFSv4StateHandler stateHandler = new NFSv4StateHandler(2, instanceId);
        NFS4State state = createClient(stateHandler).createState(_owner);
        assertEquals("Invalid instance id returned", instanceId, NFSv4StateHandler.getInstanceId(state.stateid()));
    }

}
