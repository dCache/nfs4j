package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.deviceid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_fh4;

public class Stripe {


    private final long _offset;
    private final long _len;
    private final deviceid4 _deviceId;
    private final nfs_fh4 _fh;


    /**
     * Construct new stripe unit.
     *
     * @param deviceId
     * @param fh
     * @param len
     * @param offset
     * @throws IllegalArgumentException if device id or fh is null
     */
    public Stripe(deviceid4 deviceId, nfs_fh4 fh, long len, long offset)
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




}
