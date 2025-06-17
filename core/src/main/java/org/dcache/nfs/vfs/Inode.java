/*
 * Copyright (c) 2009 - 2024 Deutsches Elektronen-Synchroton,
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

import java.util.Arrays;

public class Inode extends FileHandle {
    /**
     * This constructor will become marked {@code protected} in a future version.
     *
     * @param bytes The VFS-specific bytes.
     */
    @Deprecated
    public Inode(byte[] bytes) {
        super(bytes);
    }

    @Deprecated(forRemoval = true)
    public Inode(FileHandle fh) {
        this(fh.bytes());
    }

    Inode(int generation, int exportIdx, int type, byte[] fs_opaque) {
        super(generation, exportIdx, type, fs_opaque);
    }

    public static Inode forFileHandle(FileHandle fh) {
        if (fh instanceof Inode) {
            return ((Inode) fh);
        }
        return new Inode(fh.bytes());
    }

    public static Inode forNfsHandle(byte[] bytes) {
        return new Inode(bytes);
    }

    public static Inode forFile(byte[] bytes) {
        return new FileHandle.FileHandleBuilder().buildInode(bytes);
    }

    public byte[] getFileId() {
        return getFsOpaque();
    }

    public byte[] toNfsHandle() {
        return bytes();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Inode other = (Inode) obj;
        return Arrays.equals(bytes(), other.bytes());
    }

    public boolean isPseudoInode() {
        return getType() == 1;
    }

    public int exportIndex() {
        return getExportIdx();
    }

    public int handleVersion() {
        return getVersion();
    }
}
