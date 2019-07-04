/*
 * Copyright (c) 2009 - 2019 Deutsches Elektronen-Synchroton,
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

import java.io.IOException;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

import org.dcache.nfs.v4.xdr.GETDEVICEINFO4args;
import org.dcache.nfs.v4.xdr.GETDEVICELIST4args;
import org.dcache.nfs.v4.xdr.LAYOUTCOMMIT4args;
import org.dcache.nfs.v4.xdr.LAYOUTGET4args;
import org.dcache.nfs.v4.xdr.LAYOUTRETURN4args;
import org.dcache.nfs.v4.xdr.device_addr4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.layouttype4;

/**
 * An interface to manage pNFS layouts.
 */
public interface NFSv41DeviceManager {

    /**
     * Get a file {@link Layout}. An state is associated with the returned layout.
     * The layout is considered to be in use until <code>layoutReturn</code>
     * method is not called.
     *
     * @param context the context of the nfs request.
     * @param args layout get operation arguments.
     * @return pnfs file layout.
     * @throws IOException if NFS error or some other I/O error occurs.
     */
    public Layout layoutGet(CompoundContext context, LAYOUTGET4args args)
            throws IOException;

    /**
     * Get storage device address information for the specified device {@link deviceid4}.
     *
     * @param context the context of the nfs request.
     * @param args get device info operation arguments.
     * @return device address
     * @throws IOException if NFS error or some other I/O error occurs.
     */
    public device_addr4 getDeviceInfo(CompoundContext context, GETDEVICEINFO4args args) throws IOException;

    /**
     * Get list of all device IDs used by server.
     *
     * @param context the context of the nfs request.
     * @param args get device list operation arguments.
     * @return list of devices.
     * @throws IOException if NFS error or some other I/O error occurs.
     */
    public List<deviceid4> getDeviceList(CompoundContext context, GETDEVICELIST4args args) throws IOException;

    /**
     * Release layout associated with state id. The returned layout specific
     * {@code body} used by client to report IO errors or layout usage statistic.
     *
     * @param context the context of the nfs request.
     * @param args layout return operation arguments.
     * @throws IOException if NFS error or some other I/O error occurs.
     */
    public void layoutReturn(CompoundContext context, LAYOUTRETURN4args args) throws IOException;

    /**
     * Returns the array of layout types supported by this device manager.
     * @return supported layout types.
     */
    public Set<layouttype4> getLayoutTypes();
}
