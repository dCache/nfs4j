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

public class FSINFO3resok implements XdrAble {
    public post_op_attr obj_attributes;
    public uint32 rtmax;
    public uint32 rtpref;
    public uint32 rtmult;
    public uint32 wtmax;
    public uint32 wtpref;
    public uint32 wtmult;
    public uint32 dtpref;
    public size3 maxfilesize;
    public nfstime3 time_delta;
    public uint32 properties;

    public FSINFO3resok() {
    }

    public FSINFO3resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        obj_attributes.xdrEncode(xdr);
        rtmax.xdrEncode(xdr);
        rtpref.xdrEncode(xdr);
        rtmult.xdrEncode(xdr);
        wtmax.xdrEncode(xdr);
        wtpref.xdrEncode(xdr);
        wtmult.xdrEncode(xdr);
        dtpref.xdrEncode(xdr);
        maxfilesize.xdrEncode(xdr);
        time_delta.xdrEncode(xdr);
        properties.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        obj_attributes = new post_op_attr(xdr);
        rtmax = new uint32(xdr);
        rtpref = new uint32(xdr);
        rtmult = new uint32(xdr);
        wtmax = new uint32(xdr);
        wtpref = new uint32(xdr);
        wtmult = new uint32(xdr);
        dtpref = new uint32(xdr);
        maxfilesize = new size3(xdr);
        time_delta = new nfstime3(xdr);
        properties = new uint32(xdr);
    }

}
// End of FSINFO3resok.java
