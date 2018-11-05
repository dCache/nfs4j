/*
 * Copyright (c) 2018 Deutsches Elektronen-Synchroton,
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

/**
 * A {@link ClientRecoveryStore} that doesn't stores records.
 */
public class EphemeralClientRecoveryStore implements ClientRecoveryStore {

    @Override
    public void addClient(byte[] client) {}

    @Override
    public void reclaimClient(byte[] client) {}

    @Override
    public void removeClient(byte[] client) {}

    @Override
    public boolean waitingForReclaim() {
        return false;
    }

    @Override
    public void wantReclaim(byte[] client) {}

    @Override
    public void reclaimComplete() {}

    @Override
    public void close() {}
}
