/*
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
package org.dcache.chimera.nfs.vfs;

import java.io.IOException;
import java.util.List;
import org.dcache.chimera.FsStat;

public interface VirtualFileSystem {

    Inode create(Inode parent, Inode.Type type, String path, int uid, int gid, int mode) throws IOException;

    FsStat getFsStat() throws IOException;

    Inode getRootInode() throws IOException;

    Inode inodeOf(final byte[] fh) throws IOException;

    Inode inodeOf(Inode parent, String path) throws IOException;

    Inode link(Inode parent, Inode link, String path, int uid, int gid) throws IOException;

    List<DirectoryEntry> list(Inode inode) throws IOException;

    Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws IOException;

    void move(Inode src, String oldName, Inode dest, String newName) throws IOException;

    Inode parentOf(Inode inode) throws IOException;

    int read(Inode inode, byte[] data, long offset, int count) throws IOException;

    String readlink(Inode inode) throws IOException;

    boolean remove(Inode parent, String path) throws IOException;

    Inode symlink(Inode parent, String path, String link, int uid, int gid, int mode) throws IOException;

    int write(Inode inode, byte[] data, long offset, int count) throws IOException;
}
