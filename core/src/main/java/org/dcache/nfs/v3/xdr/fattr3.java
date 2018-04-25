/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v3.xdr;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;

public class fattr3 implements XdrAble {
    public int type;
    public mode3 mode;
    public uint32 nlink;
    public uid3 uid;
    public gid3 gid;
    public size3 size;
    public size3 used;
    public specdata3 rdev;
    public uint64 fsid;
    public fileid3 fileid;
    public nfstime3 atime;
    public nfstime3 mtime;
    public nfstime3 ctime;

    public fattr3() {
    }

    public fattr3(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(type);
        mode.xdrEncode(xdr);
        nlink.xdrEncode(xdr);
        uid.xdrEncode(xdr);
        gid.xdrEncode(xdr);
        size.xdrEncode(xdr);
        used.xdrEncode(xdr);
        rdev.xdrEncode(xdr);
        fsid.xdrEncode(xdr);
        fileid.xdrEncode(xdr);
        atime.xdrEncode(xdr);
        mtime.xdrEncode(xdr);
        ctime.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        type = xdr.xdrDecodeInt();
        mode = new mode3(xdr);
        nlink = new uint32(xdr);
        uid = new uid3(xdr);
        gid = new gid3(xdr);
        size = new size3(xdr);
        used = new size3(xdr);
        rdev = new specdata3(xdr);
        fsid = new uint64(xdr);
        fileid = new fileid3(xdr);
        atime = new nfstime3(xdr);
        mtime = new nfstime3(xdr);
        ctime = new nfstime3(xdr);
    }

}
// End of fattr3.java
