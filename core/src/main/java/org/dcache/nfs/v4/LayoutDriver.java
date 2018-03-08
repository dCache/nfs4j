/*
 * Copyright (c) 2015 - 2018 Deutsches Elektronen-Synchroton,
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

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.v4.xdr.device_addr4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.layout_content4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.stateid4;

/**
 *
 */
public interface LayoutDriver {

    /**
     * Returns layout type supported by this layout driver.
     * @return layout type.
     */
    layouttype4 getLayoutType();

    /**
     * Construct device address specific for layout type supported by the
     * driver. The {@code deviceAddress} must contain array of network addresses
     * pointing to the same data server.
     * @param deviceAddress array of multipath addresses.
     * @return device addresses.
     * @throws ChimeraNFSException if fails
     */
    device_addr4 getDeviceAddress(InetSocketAddress ... deviceAddress) throws ChimeraNFSException;

    /**
     * Construct a layout type specific content.
     * @param stateid open stateid used for file.
     * @param stripeSize stripe unit size
     * @param fh file handle to use when IO requests sent to the data server
     * @param deviceids ids of the data servers where segment is available.
     * @return layout content
     * @throws ChimeraNFSException if fails
     */
    layout_content4 getLayoutContent(stateid4 stateid, int stripeSize, nfs_fh4 fh, deviceid4 ... deviceids) throws ChimeraNFSException;


    /**
     * Consume the type-specific data provided on layout return.
     * @param data layout type specific data
     * @throws BadXdrException if provided data can't be decoded.
     */
    void acceptLayoutReturnData(byte[] data) throws BadXdrException;
}
