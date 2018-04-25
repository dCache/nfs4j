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

public class FSSTAT3resok implements XdrAble {
    public post_op_attr obj_attributes;
    public size3 tbytes;
    public size3 fbytes;
    public size3 abytes;
    public size3 tfiles;
    public size3 ffiles;
    public size3 afiles;
    public uint32 invarsec;

    public FSSTAT3resok() {
    }

    public FSSTAT3resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        obj_attributes.xdrEncode(xdr);
        tbytes.xdrEncode(xdr);
        fbytes.xdrEncode(xdr);
        abytes.xdrEncode(xdr);
        tfiles.xdrEncode(xdr);
        ffiles.xdrEncode(xdr);
        afiles.xdrEncode(xdr);
        invarsec.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        obj_attributes = new post_op_attr(xdr);
        tbytes = new size3(xdr);
        fbytes = new size3(xdr);
        abytes = new size3(xdr);
        tfiles = new size3(xdr);
        ffiles = new size3(xdr);
        afiles = new size3(xdr);
        invarsec = new uint32(xdr);
    }

}
// End of FSSTAT3resok.java
