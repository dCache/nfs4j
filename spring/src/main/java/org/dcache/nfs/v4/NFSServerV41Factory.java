/*
 * Copyright (c) 2017 Deutsches Elektronen-Synchroton,
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
import org.springframework.beans.factory.FactoryBean;

/**
 * A helper class to simplify Spring and NFS4J integration.
 */
public class NFSServerV41Factory implements FactoryBean<NFSServerV41> {

    private final NFSServerV41.Builder builder = new NFSServerV41.Builder();

    @Override
    public NFSServerV41 getObject() throws Exception {
        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return NFSServerV41.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void setDeviceManager(NFSv41DeviceManager deviceManager) {
        builder.withDeviceManager(deviceManager);
    }

    public void setOperationFactory(NFSv4OperationFactory operationFactory) {
        builder.withOperationFactory(operationFactory);
    }

    public void setVfs(VirtualFileSystem vfs) {
        builder.withVfs(vfs);
    }

    public void setLockManager(LockManager nlm) {
        builder.withLockManager(nlm);
    }

    public void setExportFile(ExportFile exportFile) {
        builder.withExportFile(exportFile);
    }

    public void setStateHandler(NFSv4StateHandler stateHandler) {
        builder.withStateHandler(stateHandler);
    }

}
