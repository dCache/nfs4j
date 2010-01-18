package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.stateid4;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.dcache.chimera.FsInode;

public interface NFSv41DeviceManager {

    /**
     * Get device address for a request. An state is associated with the returned device.
     * To discard the state, <i>releaseDevice</i> have to be called.
     *
     * @param inode
     * @param ioMode the value of the returned layout iomode. A ioMode of either
     *      LAYOUTIOMODE4_READ or LAYOUTIOMODE4_RW MUST be returned.
     * @param clientIp
     * @param stateid reflecting the correspondingly valid open, byte-range lock,
     *      or delegation stateid.
     * @return
     * @throws IOException
     */
    public NFS4IoDevice getIoDevice(FsInode inode, int ioMode, InetAddress clientIp, stateid4 stateid)
            throws IOException;

    /**
     *
     * Add a new device into list of known devices.
     *
     * @param device
     * @param ioMode
     */
    public void addIoDevice(NFS4IoDevice device, int ioMode);

    /**
     * Get device by deviceId.
     *
     * @param deviceId
     * @return NFS io device address
     */
    public NFS4IoDevice getIoDevice(DeviceID deviceId);

    /**
     * Get list of all registered devices.
     *
     * @return list of devices
     */
    public List<NFS4IoDevice> getIoDeviceList();

    /**
     * Release layout associated by state id.
     *
     * @param layout stateid
     */
    public void releaseDevice(stateid4 stateid);
}
