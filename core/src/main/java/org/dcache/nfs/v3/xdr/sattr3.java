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

public class sattr3 implements XdrAble {
    public set_mode3 mode;
    public set_uid3 uid;
    public set_gid3 gid;
    public set_size3 size;
    public set_atime atime;
    public set_mtime mtime;

    public sattr3() {
    }

    public sattr3(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        mode.xdrEncode(xdr);
        uid.xdrEncode(xdr);
        gid.xdrEncode(xdr);
        size.xdrEncode(xdr);
        atime.xdrEncode(xdr);
        mtime.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        mode = new set_mode3(xdr);
        uid = new set_uid3(xdr);
        gid = new set_gid3(xdr);
        size = new set_size3(xdr);
        atime = new set_atime(xdr);
        mtime = new set_mtime(xdr);
    }

}
// End of sattr3.java
