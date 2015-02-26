/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.xdr;
import org.dcache.xdr.*;
import java.io.IOException;
import org.dcache.nfs.status.InvalException;

public class offset4 extends uint64_t {

    public offset4() {
    }

    public offset4(long value) {
        super(value);
    }

    public offset4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        super(xdr);
    }

    /**
     * Checks whatever extending a file from this offset with to a given
     * length will overflow NFS4_UINT64_MAX.
     *
     * <p>
     * NOTICE: as Java does not supports unsigned longs, check is performed for
     * {@link Long.MAX_VALUE}.
     * </p>
     *
     * @param length to verify
     * @param msg included into exception
     * @throws InvalException if offset + length will overflow {@link Long.MAX_VALUE}
     */
    public void checkOverflow(length4 length, String msg) throws InvalException {
        if (Long.MAX_VALUE - value < length.value) {
            throw new InvalException(msg);
        }
    }
}
// End of offset4.java
