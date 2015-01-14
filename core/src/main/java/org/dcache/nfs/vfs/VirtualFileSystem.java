/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.vfs;

import java.io.IOException;
import java.util.List;
import javax.security.auth.Subject;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.v4.xdr.stable_how4;

public interface VirtualFileSystem {

    int access(Inode inode, int mode) throws IOException;

    Inode create(Inode parent, Stat.Type type, String path, Subject subject, int mode) throws IOException;

    FsStat getFsStat() throws IOException;

    Inode getRootInode() throws IOException;

    Inode lookup(Inode parent, String path) throws IOException;

    Inode link(Inode parent, Inode link, String path, Subject subject) throws IOException;

    List<DirectoryEntry> list(Inode inode) throws IOException;

    Inode mkdir(Inode parent, String path, Subject subject, int mode) throws IOException;

    boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException;

    Inode parentOf(Inode inode) throws IOException;

    int read(Inode inode, byte[] data, long offset, int count) throws IOException;

    String readlink(Inode inode) throws IOException;

    void remove(Inode parent, String path) throws IOException;

    Inode symlink(Inode parent, String path, String link, Subject subject, int mode) throws IOException;

    WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel) throws IOException;

    void commit(Inode inode, long offset, int count) throws IOException;

    Stat getattr(Inode inode) throws IOException;

    void setattr(Inode inode, Stat stat) throws IOException;

    nfsace4[] getAcl(Inode inode) throws IOException;

    void setAcl(Inode inode, nfsace4[] acl) throws IOException;

    boolean hasIOLayout(Inode inode) throws IOException;

    AclCheckable getAclCheckable();

    NfsIdMapping getIdMapper();

    public static class WriteResult {
        private final int bytesWritten;
        private final StabilityLevel stabilityLevel;

        public WriteResult(StabilityLevel stabilityLevel, int bytesWritten) {
            this.stabilityLevel = stabilityLevel;
            this.bytesWritten = bytesWritten;
        }

        public int getBytesWritten() {
            return bytesWritten;
        }

        public StabilityLevel getStabilityLevel() {
            return stabilityLevel;
        }
    }

    //NOTE - stability values and ordinals are the same for nfs 3 and 4
    public static enum StabilityLevel {
        UNSTABLE, DATA_SYNC, FILE_SYNC;

        public static StabilityLevel fromStableHow(int stableHowValue) {
            switch (stableHowValue) {
                case stable_how4.UNSTABLE4:
                    return UNSTABLE;
                case stable_how4.DATA_SYNC4:
                    return DATA_SYNC;
                case stable_how4.FILE_SYNC4:
                    return FILE_SYNC;
                default:
                    throw new IllegalArgumentException("unhandled stability value "+stableHowValue);
            }
        }

        public int toStableHow() {
            return ordinal();
        }
    }
}
