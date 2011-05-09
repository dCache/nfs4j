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

import java.util.List;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.FsStat;

public interface VirtualFileSystem {

    Inode create(Inode parent, Inode.Type type, String path, int uid, int gid, int mode) throws ChimeraFsException;

    FsStat getFsStat() throws ChimeraFsException;

    Inode getRootInode() throws ChimeraFsException;

    Inode inodeOf(final byte[] fh);

    Inode inodeOf(Inode parent, String path) throws ChimeraFsException;

    Inode link(Inode parent, Inode link, String path, int uid, int gid) throws ChimeraFsException;

    List<DirectoryEntry> list(Inode inode) throws ChimeraFsException;

    Inode mkdir(Inode parent, String path, int uid, int gid, int mode) throws ChimeraFsException;

    void move(Inode src, String oldName, Inode dest, String newName) throws ChimeraFsException;

    Inode parentOf(Inode inode) throws ChimeraFsException;

    int read(Inode inode, byte[] data, long offset, int count) throws ChimeraFsException;

    String readlink(Inode inode) throws ChimeraFsException;

    boolean remove(Inode parent, String path) throws ChimeraFsException;

    Inode symlink(Inode parent, String path, String link, int uid, int gid, int mode) throws ChimeraFsException;

    int write(Inode inode, byte[] data, long offset, int count) throws ChimeraFsException;
}
