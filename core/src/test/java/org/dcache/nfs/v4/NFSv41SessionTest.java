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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadSlotException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.dcache.nfs.v4.NfsTestUtils.createClient;
/**
 *
 */
public class NFSv41SessionTest {

    private NFSv41Session _session;
    private NFS4Client _client;

    @Before
    public void setUp() throws UnknownHostException, ChimeraNFSException {
        _client = createClient();
        _session = _client.createSession(1, 10, 1, 8, 8);
    }

    @Test
    public void testHighestSlotEmpty() {
        assertEquals("invalid highest slot id on empty session", -1, _session.getHighestUsedSlot());
    }

    @Test
    public void testHighestSlotUsed() throws ChimeraNFSException {

        int slotToUse = _session.getHighestSlot() / 2;
        List<nfs_resop4> reply = new ArrayList<>();
        _session.updateSlotCache(slotToUse, reply);

        assertEquals("invalid highest slot id on empty session", slotToUse, _session.getHighestUsedSlot());
    }

    @Test(expected=BadSlotException.class)
    public void testInvalidSlotAccess() throws ChimeraNFSException {

        int slotToUse = _session.getHighestSlot() + 1;
        List<nfs_resop4> reply = new ArrayList<>();
        _session.updateSlotCache(slotToUse, reply);
    }

    @Test
    public void testSessionRemove() throws ChimeraNFSException {
        assertTrue(_client.hasSessions());
        _client.removeSession(_session);
        assertFalse(_client.hasSessions());
    }
}
