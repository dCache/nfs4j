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

import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.nfs.v4.xdr.nfsace4;
import org.dcache.chimera.posix.Stat;

public interface Inode {

    enum Type {

        LEGACY,
        REGULAR,
        DIRECTORY,
        SYMLINK,
        CHAR,
        BLOCK,
        FIFO,
        SOCK
    }

    public byte[] toFileHandle();

    public boolean exists();

    Stat stat() throws ChimeraFsException;

    Stat statCache() throws ChimeraFsException;

    int id();

    void setSize(long size) throws ChimeraFsException;

    void setUID(int id) throws ChimeraFsException;

    void setGID(int id) throws ChimeraFsException;

    void setATime(long time) throws ChimeraFsException;

    void setMTime(long time) throws ChimeraFsException;

    void setCTime(long time) throws ChimeraFsException;

    void setMode(int size) throws ChimeraFsException;

    nfsace4[] getAcl() throws ChimeraFsException;

    void setAcl(nfsace4[] acl) throws ChimeraFsException;

    Type type();
}
