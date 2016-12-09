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

import java.util.Collections;
import java.util.List;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.status.SeqMisorderedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SessionSlot {

    private static final Logger _log = LoggerFactory.getLogger(SessionSlot.class);

    private int _sequence;
    private List<nfs_resop4> _reply;

    public SessionSlot() {
       _sequence = 0;
    }

    /**
     * Acquire the session cache slot for a given sequence number. The
     * value of {@code sequence} is compared to the previous sequence id, with
     * three possible outcomes:
     * <ul>
     *   <li> If the provided sequence id and the previous sequence id are the
     *         same then the request is a retry.  The previous reply is returned
     *         or an empty List if no reply was recorded.
     *   <li> If the provided sequence id is one greater than the previous sequence
     *         id then this is a new request and null is returned.
     *   <li> For all other provided sequence id values a {@link SeqMisorderedException}
     *         is thrown.
     * </ul>
     *
     * @param sequence  the sequence number of the request for the reply cache entry
     * @return the list of cached replies, possibly empty or {@code null}
     * cached reply does not exist.
     * @throws SeqMisorderedException if {@code sequnce} is out of order.
     */
    List<nfs_resop4> acquire(int sequence) throws SeqMisorderedException {

        if( sequence == _sequence ) {

            _log.info("retransmit detected");
            if( _reply != null ) {
                return _reply;
            }

            return Collections.emptyList();
        }

        int validValue = _sequence + 1;
        if (sequence != validValue) {
            throw new SeqMisorderedException("disordered : v/n : " + Integer.toHexString(validValue) +
                    "/" + Integer.toHexString(sequence));
        }

        _sequence = sequence;
        _reply = null;
        return null;
    }

    void update(List<nfs_resop4> reply) {
        _reply = reply;
    }
}
