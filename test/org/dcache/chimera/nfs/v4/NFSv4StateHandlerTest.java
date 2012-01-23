package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.dcache.chimera.nfs.ChimeraNFSException;

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
