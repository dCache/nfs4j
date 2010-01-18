/*
 * $Id:NFS4IoDevice.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.device_addr4;

/** Immutable */
public class NFS4IoDevice {

	private final DeviceID _deviceId;
	private final device_addr4 _addr;


	public NFS4IoDevice(DeviceID id, device_addr4 addr) {
		_deviceId = id;
		_addr = addr;
	}

	public DeviceID getDeviceId() {
		return _deviceId;
	}

	public int getDeviceType() {
		return _addr.da_layout_type;
	}

	public device_addr4 getDeviceAddr() {
		return _addr;
	}

}
/*
 * $Log: NFS4IoDevice.java,v $
 * Revision 1.2  2006/11/16 21:00:48  tigran
 * code cleanup
 *
 * Revision 1.1  2006/09/13 16:12:36  tigran
 * added dummy device manager
 *
 */

