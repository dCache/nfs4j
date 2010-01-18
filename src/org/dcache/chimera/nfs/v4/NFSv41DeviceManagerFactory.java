package org.dcache.chimera.nfs.v4;

public class NFSv41DeviceManagerFactory {

	private final static NFSv41DeviceManager DEFAULT_MANAGER = new DeviceManager();
	private static NFSv41DeviceManager _MANAGER = DEFAULT_MANAGER;

	private NFSv41DeviceManagerFactory() {
	    // no instance allowed
	}

    public static void setDeviceManager(NFSv41DeviceManager deviceManager) {
        _MANAGER = deviceManager;
    }

	public static NFSv41DeviceManager getDeviceManager() {
		return _MANAGER;
	}

}
