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

import java.io.Closeable;
import org.dcache.nfs.status.NoGraceException;
import org.dcache.nfs.status.ReclaimBadException;

/**
 * Interface to a store that keeps record of know client to allow opens and locks
 * recovery after server reboot.
 *
 * @see: https://tools.ietf.org/html/rfc5661#section-8.4.2.1
 */
public interface ClientRecoveryStore extends Closeable {

    /**
     * Add client record into recovery store. An existing record for provided
     * {@code client} will be updated.
     *
     * @param client client's unique identifier.
     */
    void addClient(byte[] client);

    /**
     * Indicates that {@code owner} have finished reclaim procedure. This method
     * is called by client even it there was no stated to reclaim.
     *
     * @param client client's unique identifier.
     */
    void reclaimClient(byte[] client);

    /**
     * Remove client record from recovery store. Called when client record is
     * destroyed due to expiry or destroy (unmount).
     *
     * @param client client's unique identifier.
     */
    void removeClient(byte[] client);

    /**
     * Checks this client store for a pending reclaim. The does not expects any
     * reclaims when grace period is expired or all previously existing clients
     * have complete their reclaims.
     *
     * @return true if store expects reclaims from previously existing clients.
     */
    boolean waitingForReclaim();

    /**
     * Check that client is eligible to reclaim states.
     *
     * @param client client's unique identifier.
     *
     * @throws NoGraceException is grace period is over
     * @throws ReclaimBadException client's prevision state can't be detected.
     */
    void wantReclaim(byte[] client) throws NoGraceException, ReclaimBadException;

    /**
     * Indicate the end of grace period. The records for client's that did not
     * showed up during grace period will be removed.
     */
    void reclaimComplete();

}
