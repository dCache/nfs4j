package org.dcache.chimera.nfs.v4;

import java.util.Arrays;

/**
 * Helper ( wrapper ) class for byte[] based deviceid4.
 * required to be able to use  byte[] as a key in the Collections
 *
 */
public class DeviceID {


    private final byte[] _id;

    public DeviceID(byte[] id) {
        _id = new byte[id.length];
        System.arraycopy(id, 0, _id, 0, id.length);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(_id);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final DeviceID other = (DeviceID) obj;
        if (!Arrays.equals(_id, other._id)) return false;
        return true;
    }

    public byte[] getId() {
        byte[] id =  new byte[_id.length];
        System.arraycopy(_id, 0, id, 0, _id.length);
        return id;
    }



}
