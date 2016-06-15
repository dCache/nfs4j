/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.client.CloseStub;
import org.dcache.nfs.v4.xdr.clientid4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.state_owner4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.junit.Before;
import org.junit.Test;

import org.dcache.nfs.vfs.Inode;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class NFS4ClientTest {

    private NFSv4StateHandler stateHandler;
    private NFS4Client nfsClient;
    private state_owner4 owner;

    @Before
    public void setUp() throws UnknownHostException, ChimeraNFSException {
        stateHandler = new NFSv4StateHandler();
        nfsClient = createClient(stateHandler);
        owner = nfsClient.asStateOwner();
    }

    @Test
    public void testStateCleanOnOpenCloseV41() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        NFSv41Session session = nfsClient.createSession(1, 2, 1, 8, 8);
        NFS4State state = nfsClient.createState(owner);

        nfs_argop4 close_args = CloseStub.generateRequest(state.stateid());
        OperationCLOSE CLOSE = new OperationCLOSE(close_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CLOSE);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .withMinorversion(1)
                .withDeviceManager(mock(NFSv41DeviceManager.class))
                .build();

        context.currentInode( Inode.forFile( new byte[] {}));
        context.setSession(session);

        AssertNFS.assertNFS(CLOSE, context, result, nfsstat.NFS_OK);
        assertFalse("client state not cleaned", nfsClient.hasState());
    }

    @Test
    public void testStateCleanOnOpenCloseV40() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        NFS4State state = nfsClient.createState(owner);

        nfs_argop4 close_args = CloseStub.generateRequest(state.stateid());
        OperationCLOSE CLOSE = new OperationCLOSE(close_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CLOSE);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .withMinorversion(0)
                .build();

        context.currentInode(Inode.forFile(new byte[]{}));

        AssertNFS.assertNFS(CLOSE, context, result, nfsstat.NFS_OK);
        assertFalse("client state not cleaned", nfsClient.hasState());
    }

    @Test
    public void testInitiallyNoStates() throws ChimeraNFSException {
        assertFalse(nfsClient.hasState());
    }

    @Test
    public void testAttacheDetachState() throws ChimeraNFSException {

        state_owner4 otherOwner = new state_owner4();
        otherOwner.clientid = new clientid4(123);
        otherOwner.owner = "someOtherOwner".getBytes(StandardCharsets.UTF_8);
        NFS4State state = new NFS4State(otherOwner, new stateid4(new byte[] {}, 0));

        nfsClient.attachState(state);
        assertTrue(nfsClient.hasState());

        nfsClient.detachState(state);
        assertFalse(nfsClient.hasState());
    }

    @Test
    public void testCreateState() throws ChimeraNFSException {
        NFS4State state = nfsClient.createState(owner);
        assertTrue(nfsClient.hasState());
    }

}

