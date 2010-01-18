package org.dcache.chimera.nfs.v4;

import java.util.Arrays;
import org.dcache.chimera.nfs.v4.xdr.deviceid4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;

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
        if( !(obj instanceof DeviceID)) return false;
        
        final DeviceID other = (DeviceID) obj;
        return Arrays.equals(_id, other._id);
    }

    public byte[] getId() {
        byte[] id =  new byte[_id.length];
        System.arraycopy(_id, 0, id, 0, _id.length);
        return id;
    }

    public static DeviceID valueOf(deviceid4 id) {
        return new DeviceID(id.value);
    }

    public static DeviceID valueOf(int id) {
        return new DeviceID(id2deviceid(id));
    }

    private static byte[] id2deviceid(int id) {

        byte[] buf = Integer.toString(id).getBytes();
        byte[] devData = new byte[nfs4_prot.NFS4_DEVICEID4_SIZE];

        int len = Math.min(buf.length, nfs4_prot.NFS4_DEVICEID4_SIZE);
        System.arraycopy(buf, 0, devData, 0, len);

        return devData;
    }

    public deviceid4 toDeviceid4() {
        return new deviceid4(_id);
    }

    @Override
    public String toString() {
        return Arrays.toString(_id);
    }

}
