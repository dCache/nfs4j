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

    Stat stat() throws IOException;

    Stat statCache() throws IOException;

    long id();

    void setSize(long size) throws IOException;

    void setUID(int id) throws IOException;

    void setGID(int id) throws IOException;

    void setATime(long time) throws IOException;

    void setMTime(long time) throws IOException;

    void setCTime(long time) throws IOException;

    void setMode(int size) throws IOException;

    nfsace4[] getAcl() throws IOException;

    void setAcl(nfsace4[] acl) throws IOException;

    Type type();
}
