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

import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.nlm.LockManager;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.xdr.*;

import static java.util.Objects.requireNonNull;

public class CompoundContextBuilder {

    private LockManager lm;
    private RpcCall call = null;
    private int minorversion = 0;
    private VirtualFileSystem fs = null;
    private NFSv4StateHandler stateHandler = null;
    private NFSv41DeviceManager deviceManager = null;
    private ExportFile exportFile = null;

    public CompoundContextBuilder withCall(RpcCall call) {
        this.call = call;
        return this;
    }

    public CompoundContextBuilder withDeviceManager(NFSv41DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        return this;
    }

    public CompoundContextBuilder withExportFile(ExportFile exportFile) {
        this.exportFile = exportFile;
        return this;
    }

    public CompoundContextBuilder withFs(VirtualFileSystem fs) {
        this.fs = fs;
        return this;
    }

    public CompoundContextBuilder withMinorversion(int minorversion) {
        this.minorversion = minorversion;
        return this;
    }

    public CompoundContextBuilder withStateHandler(NFSv4StateHandler stateHandler) {
        this.stateHandler = stateHandler;
        return this;
    }

    public CompoundContextBuilder withLockManager(LockManager lm) {
        this.lm = lm;
        return this;
    }

    public LockManager getLm() {
        return lm;
    }

    public RpcCall getCall() {
        return call;
    }

    public int getMinorversion() {
        return minorversion;
    }

    public VirtualFileSystem getFs() {
        return fs;
    }

    public NFSv4StateHandler getStateHandler() {
        return stateHandler;
    }

    public NFSv41DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public ExportFile getExportFile() {
        return exportFile;
    }

    public CompoundContext build() {

        requireNonNull(call);

        return new CompoundContext(this);
    }

}
