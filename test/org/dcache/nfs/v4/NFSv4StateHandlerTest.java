/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.v4.NFS4State;
import org.dcache.nfs.v4.NFSv4StateHandler;
import org.dcache.nfs.v4.NFS4Client;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.dcache.nfs.ChimeraNFSException;

public class NFSv4StateHandlerTest {

    private NFSv4StateHandler _stateHandler;
    private NFS4Client _client;

    @Before
    public void setUp() throws UnknownHostException {
        _stateHandler = new NFSv4StateHandler();
        _client = createClient(_stateHandler);
    }

    @Test
    public void testGetByStateId() throws Exception {
        stateid4 state = _client.createState().stateid();
        _stateHandler.getClientIdByStateId(state);
    }

    @Test
    public void testGetByVerifier() throws Exception {
        stateid4 state = _client.createState().stateid();
        assertEquals(_client, _stateHandler.getClientByVerifier(_client.verifier()));
    }

    @Test
    public void testGetByVerifierNotExists() throws Exception {
        assertNull("get not existing", _stateHandler.getClientByVerifier( new verifier4()));
    }

    @Test(expected=ChimeraNFSException.class)
    public void testGetClientNotExists() throws Exception {
        _stateHandler.getClientByID(1L);
    }

    @Test
    public void testGetClientExists() throws Exception {
         _client = createClient(_stateHandler);
        assertEquals(_client,  _stateHandler.getClientByID(_client.getId()));
    }

    @Test
    public void testUpdateLeaseTime() throws Exception {
        NFS4State state = _client.createState();
        stateid4 stateid = state.stateid();
        state.confirm();
        _stateHandler.updateClientLeaseTime(stateid);
    }

    @Test(expected=ChimeraNFSException.class)
    public void testUpdateLeaseTimeNotConfirmed() throws Exception {
        NFS4State state = _client.createState();
        stateid4 stateid = state.stateid();

        _stateHandler.updateClientLeaseTime(stateid);
    }

    @Test(expected=ChimeraNFSException.class)
    public void testUpdateLeaseTimeNotExists() throws Exception {
        stateid4 state = _client.createState().stateid();
        _stateHandler.updateClientLeaseTime(state);
    }

    static NFS4Client createClient(NFSv4StateHandler stateHandler) throws UnknownHostException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(null), 123);
        return stateHandler.createClient(address, address, "123".getBytes(), new verifier4("123".getBytes()), null);
    }
}
