/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.client;

import com.google.common.base.MoreObjects;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_fh4;

public class Stripe {


    private final long _offset;
    private final long _len;
    private final deviceid4 _deviceId;
    private final nfs_fh4 _fh;
    private final long _pattern_offset;
    private final int _unit;
    private final int _first_stripe_index;


    /**
     * Construct new stripe unit.
     *
     * @param deviceId
     * @param fh
     * @param len
     * @param offset
     * @param pattern_offset
     * @param unit
     * @param first_stripe_index
     * @throws IllegalArgumentException
     */
    public Stripe(deviceid4 deviceId, nfs_fh4 fh, long len, long offset,
                  long pattern_offset,
                  int unit, int first_stripe_index)
            throws IllegalArgumentException {

        if( deviceId == null ) {
            throw new IllegalArgumentException("device id is null");
        }

        _deviceId = deviceId;

        if( fh == null ) {
            throw new IllegalArgumentException("NFS file hadle is null");
        }

        this._fh = fh;

        /*
         * handle special case: -1 is reserved value for entire file
         */
        this._len = len == -1 ? Long.MAX_VALUE: len;
        this._offset = offset;
        _pattern_offset = pattern_offset;
        _unit = unit;
        _first_stripe_index = first_stripe_index;
    }

    public int getFirstStripeIndex() {
        return _first_stripe_index;
    }

    public long getPatternOffset() {
        return _pattern_offset;
    }

    public int getUnit() {
        return _unit;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return _offset;
    }
    /**
     * @return the len
     */
    public long getLen() {
        return _len;
    }
    /**
     * @return the deviceId
     */
    public deviceid4 getDeviceId() {
        return _deviceId;
    }
    /**
     * @return the _fh
     */
    public nfs_fh4 getFh() {
        return _fh;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_deviceId == null) ? 0 : _deviceId.hashCode());
        result = prime * result + ((_fh == null) ? 0 : _fh.hashCode());
        result = prime * result + (int) (_len ^ (_len >>> 32));
        result = prime * result + (int) (_offset ^ (_offset >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ( !(obj instanceof Stripe) ) return false;

        Stripe other = (Stripe) obj;
        // FIXME:

        if( !_deviceId.equals(other._deviceId)) return false;
        if (_len != other._len) return false;
        if (_offset != other._offset) return false;
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("offset", _offset)
                .add("length", _len)
                .add("deviceId", _deviceId)
                .add("fh", _fh)
                .add("commit", ((_unit & nfs4_prot.NFL4_UFLG_COMMIT_THRU_MDS) == 0 ? "ds" : "mds"))
                .add("blocksize", _unit & nfs4_prot.NFL4_UFLG_STRIPE_UNIT_SIZE_MASK)
                .toString();
    }
}
