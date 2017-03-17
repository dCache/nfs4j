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
package org.dcache.nfs.v4.xdr;

import com.google.common.primitives.UnsignedLong;

import java.io.IOException;

import org.dcache.xdr.*;
import org.dcache.nfs.status.InvalException;

public class offset4 extends uint64_t {

    private final static UnsignedLong MAX_UINT64 = UnsignedLong.fromLongBits(nfs4_prot.NFS4_UINT64_MAX);

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
     * Checks whatever extending a file from this offset with to a given length
     * will overflow NFS4_UINT64_MAX.
     *
     * @see #checkOverflow(long, java.lang.String)
     * @param length to verify
     * @param msg included into exception
     * @throws InvalException if offset + length will overflow
     */
    public void checkOverflow(length4 length, String msg) throws InvalException {
        checkOverflow(length.value, msg);
    }

    /**
     * Checks whatever extending a file from this offset with to a given length
     * will overflow NFS4_UINT64_MAX.
     *
     * @see #checkOverflow(long, java.lang.String)
     * @param length to verify
     * @param msg included into exception
     * @throws InvalException if offset + length will overflow
     */
    public void checkOverflow(int length, String msg) throws InvalException {
        checkOverflow((long)length, msg);
    }

    /**
     * Checks whatever extending a file from this offset with to a given length
     * will overflow NFS4_UINT64_MAX.
     *
     * @param length to verify
     * @param msg included into exception
     * @throws InvalException if offset + length will overflow
     * {@link Long.MAX_VALUE}
     */
    public void checkOverflow(long length, String msg) throws InvalException {

        if (length == nfs4_prot.NFS4_UINT64_MAX) {
            // special case - up-to-the-end
            return;
        }

        if (MAX_UINT64.minus(UnsignedLong.fromLongBits(value)).compareTo(UnsignedLong.fromLongBits(length)) < 0) {
            throw new InvalException(msg);
        }
    }
}
// End of offset4.java
