/*
 * $Id:DeviceManager.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.layouttype4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.netaddr4;
import org.dcache.chimera.nfs.v4.xdr.nfsv4_1_file_layout_ds_addr4;
import org.dcache.chimera.nfs.v4.xdr.device_addr4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.multipath_list4;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.chimera.FsInode;
import org.dcache.xdr.XdrBuffer;

/**
 *
 * the instance of this class have to ask Pool Manager for a pool and return it
 * to the client.
 *
 */

public class DeviceManager implements NFSv41DeviceManager {


    private static final Logger _log = Logger.getLogger(DeviceManager.class.getName());

    /* hack for multiple pools */
    Random _poolManager = new Random();

    private final Map<DeviceID, NFS4IoDevice> _deviceMap = new HashMap<DeviceID, NFS4IoDevice>();

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.chimera.nfsv4.NFSv41DeviceManager#getIoDeviceId(org.dcache.chimera.FsInode,
     *      int, java.net.InetAddress)
     */
    public NFS4IoDevice getIoDevice(FsInode inode, int ioMode, InetAddress clientIp, stateid4 stateid) {

        int id = _poolManager.nextInt(256);
        ++id; /* 0 is reserved */
        byte[] data = id2deviceid(id);

        _log.log(Level.FINEST, "generating new device: {0} ({1}) for stateid {2}",
                new Object[] {Arrays.toString(data), id, stateid}
        );

        InetAddress addr = null;

        try {
            addr = InetAddress.getByName("127.0.0.1");
        }catch(UnknownHostException e) {
            // fine with me
        }

        //hard coded for now
        InetSocketAddress[] addresses = new InetSocketAddress[1];
        addresses[0] = new InetSocketAddress(addr, 2052);
        device_addr4 deviceAddr = deviceAddrOf( addresses );

        NFS4IoDevice newDevice = new NFS4IoDevice(id2deviceid(id) , deviceAddr);

        addIoDevice(newDevice, ioMode);


        return newDevice;
    }

    /**
     * Create IO dive and maps to given id
     *
     * @param device
     *            id to be mapped
     * @param ioMode
     */
    public void addIoDevice(NFS4IoDevice device, int ioMode ) {

        _log.log(Level.FINEST, "add device: {0}", Arrays.toString(device.getDeviceId()) );

        synchronized (_deviceMap) {

            DeviceID deviceId = new DeviceID(device.getDeviceId());
            if (!_deviceMap.containsKey(deviceId) ) {
                _deviceMap.put(deviceId, device);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.chimera.nfsv4.NFSv41DeviceManager#getIoDevice(int)
     */
    public NFS4IoDevice getIoDevice(byte[] deviceId) {

        _log.log(Level.FINEST, "lookup for device: {0}", Arrays.toString(deviceId) );

        NFS4IoDevice device = null;
        synchronized (_deviceMap) {
            device = _deviceMap.get(new DeviceID(deviceId));
        }
        return device;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.chimera.nfsv4.NFSv41DeviceManager#getIoDeviceList()
     */
    public List<NFS4IoDevice> getIoDeviceList() {
        List<NFS4IoDevice> deviceList = new ArrayList<NFS4IoDevice>();
        synchronized (_deviceMap) {
            deviceList.addAll(_deviceMap.values());
        }
        for(NFS4IoDevice device : deviceList ) {
            _log.log(Level.FINEST, "known device: {0}", Arrays.toString(device.getDeviceId()) );
        }
        return deviceList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.chimera.nfsv4.NFSv41DeviceManager#releaseDevice()
     */
    public void releaseDevice(stateid4 stateid) {
        // I'am fine
        _log.log(Level.FINEST, "release device for stateid {0}", stateid );
    }

    public static byte[] id2deviceid(int id) {


        byte[] puffer = Integer.toString(id).getBytes();
        byte[] devData = new byte[nfs4_prot.NFS4_DEVICEID4_SIZE];

        int len = puffer.length >  nfs4_prot.NFS4_DEVICEID4_SIZE? nfs4_prot.NFS4_DEVICEID4_SIZE : puffer.length;
        System.arraycopy(puffer, 0, devData, 0, len);

        return devData;

    }


    /**
     * Create a mulipath based NFSv4.1 file layout address.
     *
     * @param deviceAddress
     * @return device address
     */
    public static device_addr4 deviceAddrOf(InetSocketAddress[] deviceAddress) {

        nfsv4_1_file_layout_ds_addr4 file_type = new nfsv4_1_file_layout_ds_addr4();


        file_type.nflda_multipath_ds_list = new multipath_list4[deviceAddress.length];

        for( int i = 0; i < deviceAddress.length; i++ ) {

            file_type.nflda_multipath_ds_list[i] = new multipath_list4();
            file_type.nflda_multipath_ds_list[i].value = new netaddr4[1];

            file_type.nflda_multipath_ds_list[i].value[0] = new netaddr4();
            file_type.nflda_multipath_ds_list[i].value[0].na_r_addr =
                                HimeraNFS4Utils.inetAddress2rAddr(deviceAddress[i]);
            file_type.nflda_multipath_ds_list[i].value[0].na_r_netid = "tcp";

        }

        file_type.nflda_stripe_indices = new uint32_t[1];
        file_type.nflda_stripe_indices[0] = new uint32_t();
        file_type.nflda_stripe_indices[0].value = 0;

        XdrEncodingStream xdr = new XdrBuffer(128);
        try {
            xdr.beginEncoding();
            file_type.xdrEncode(xdr);
            xdr.endEncoding();
        }catch(OncRpcException e) {
            // should never happen
            // TODO: in case of - panic!
        }catch(IOException e) {
            // should never happen
            // TODO: in case of - panic!
        }

        ByteBuffer body = xdr.body();
        byte[] retBytes = new byte[body.limit()] ;
        body.get(retBytes);

        device_addr4 addr = new device_addr4();
        addr.da_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
        addr.da_addr_body = retBytes;

        return addr;

    }
}
