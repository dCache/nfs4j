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

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.stateid4;

public class Stateids {

    private Stateids() {
    }
    private final static stateid4 CURRENT_STATEID =
            new stateid4(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 1);
    private final static stateid4 INVAL_STATEID =
            new stateid4(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, nfs4_prot.NFS4_UINT32_MAX);

    public static stateid4 uptodateOf(stateid4 stateid) {
        return new stateid4(stateid.other, 0);
    }

    public static stateid4 currentStateId() {
        return CURRENT_STATEID;
    }

    public static stateid4 invalidStateId() {
        return INVAL_STATEID;
    }

    public static void checkStateId(stateid4 expected, stateid4 stateid) throws ChimeraNFSException {
        if (expected.seqid.value > stateid.seqid.value) {
            throw new ChimeraNFSException(nfsstat.NFSERR_OLD_STATEID, "old stateid");
        }

        if (expected.seqid.value < stateid.seqid.value) {
            throw new ChimeraNFSException(nfsstat.NFSERR_BAD_STATEID, "bad stateid");
        }
    }
}
