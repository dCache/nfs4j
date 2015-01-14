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

import java.util.Arrays;

public class Inode {

    FileHandle fh;

    public Inode(byte[] bytes) {
        this(new FileHandle(bytes));
    }

    public Inode(FileHandle h) {
        fh = h;
    }

    public static Inode forFile(byte[] bytes) {
        return new Inode(new FileHandle.FileHandleBuilder().build(bytes));
    }

    public byte[] getFileId() {
        return fh.getFsOpaque();
    }

    public byte[] toNfsHandle() {
        return fh.bytes();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(fh.bytes());
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
        return Arrays.equals(fh.bytes(), other.fh.bytes());
    }

    public boolean isPesudoInode() {
        return fh.getType() == 1;
    }

    public int exportIndex() {
        return fh.getExportIdx();
    }

    public int handleVersion() {
        return fh.getVersion();
    }

    @Override
    public String toString() {
        return fh.toString();
    }
}
