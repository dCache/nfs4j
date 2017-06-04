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

import org.dcache.nfs.v4.xdr.layout4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.device_addr4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.LayoutUnavailableException;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.utils.Bytes;
import org.dcache.utils.net.InetSocketAddresses;

/**
 *
 * the instance of this class have to ask Pool Manager for a pool and return it
 * to the client.
 *
 */

public class DeviceManager implements NFSv41DeviceManager {

    /*
     * reserved device for IO through MDS (for pnfs dot files)
     */
    private static final deviceid4 MDS_ID = deviceidOf(0);

    private static final Logger _log = LoggerFactory.getLogger(DeviceManager.class);

    /* hack for multiple pools */
    private final Random _deviceIdGenerator = new Random();

    private final Map<deviceid4, device_addr4> _deviceMap =
            new ConcurrentHashMap<>();

    private InetSocketAddress[] _knownDataServers;
    private final LayoutDriver layoutDriver = new NfsV41FileLayoutDriver();

    /**
     * Set configures data servers. Each string represents a dataserver
     * as <code>IP:port</code>
     * @param servers
     */
    public void setDataservers(String[] servers) {
        _knownDataServers = new InetSocketAddress[servers.length];
        for(int i = 0; i < servers.length; i++) {
            _knownDataServers[i] = InetSocketAddresses.inetAddressOf(servers[i]);
        }
    }

    private int nextDeviceID() {
        /* 0 is reserved  for MDS */
        return _deviceIdGenerator.nextInt(255) + 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.nfsv4.NFSv41DeviceManager#layoutGet(CompoundContext context,
     *              Inode inode, int layoutType, int ioMode, stateid4 stateid)
     */
    @Override
    public Layout layoutGet(CompoundContext context, Inode inode, int layoutType, int ioMode, stateid4 stateid)
            throws IOException {

        device_addr4 deviceAddr;
        deviceid4 deviceId;

        if (layoutType != layoutDriver.getLayoutType()) {
            throw new LayoutUnavailableException("layout not supported");
        }

        if (!context.getFs().hasIOLayout(inode)) {
            deviceId = MDS_ID;
        } else {

            if(_knownDataServers.length == 0) {
                throw new LayoutUnavailableException("No dataservers available");
            }
            int id = nextDeviceID();
            deviceId = deviceidOf(id);

            _log.debug("generating new device: {} ({}) for stateid {}",
                    deviceId, id, stateid);

            deviceAddr = layoutDriver.getDeviceAddress(_knownDataServers);

            _deviceMap.put(deviceId, deviceAddr);
        }

        nfs_fh4 fh = new nfs_fh4(context.currentInode().toNfsHandle());

        //  -1 is special value, which means entire file
        layout4 layout = new layout4();
        layout.lo_iomode = ioMode;
        layout.lo_offset = new offset4(0);
        layout.lo_length = new length4(nfs4_prot.NFS4_UINT64_MAX);
        layout.lo_content = layoutDriver.getLayoutContent(stateid,  NFSv4Defaults.NFS4_STRIPE_SIZE, fh, deviceId);

        return  new Layout(true, stateid, new layout4[]{layout});
    }


    /*
     * (non-Javadoc)
     *
     * @see org.dcache.nfsv4.NFSv41DeviceManager#getDeviceInfo(CompoundContext context, deviceid4 deviceId)
     */
    @Override
    public device_addr4 getDeviceInfo(CompoundContext context, deviceid4 deviceId, int layoutType) throws ChimeraNFSException {

        _log.debug("lookup for device: {}", deviceId );
        /* in case of MDS access we return the same interface which client already connected to */
        if(deviceId.equals(MDS_ID)) {
            return layoutDriver.getDeviceAddress(context.getLocalSocketAddress());
        }

        return  _deviceMap.get(deviceId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.nfsv4.NFSv41DeviceManager#getDeviceList()
     */
    @Override
    public List<deviceid4> getDeviceList(CompoundContext context) {
        return new ArrayList<>(_deviceMap.keySet());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.nfsv4.NFSv41DeviceManager#layoutReturn()
     */
    @Override
    public void layoutReturn(CompoundContext context, stateid4 stateid) {
        // I'am fine
        _log.debug( "release device for stateid {}", stateid );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dcache.nfsv4.NFSv41DeviceManager#getLayoutTypes()
     */
    @Override
    public int[] getLayoutTypes() {
        return new int[]{layoutDriver.getLayoutType()};
    }

    private static deviceid4 deviceidOf(int id) {
        byte[] deviceidBytes = new byte[nfs4_prot.NFS4_DEVICEID4_SIZE];
        Bytes.putInt(deviceidBytes, 0, id);

        return new deviceid4(deviceidBytes);
    }
}
