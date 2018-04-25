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

public class mknoddata3 implements XdrAble {
    public int type;
    public devicedata3 device;
    public sattr3 pipe_attributes;

    public mknoddata3() {
    }

    public mknoddata3(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(type);
        switch ( type ) {
        case ftype3.NF3CHR:
        case ftype3.NF3BLK:
            device.xdrEncode(xdr);
            break;
        case ftype3.NF3SOCK:
        case ftype3.NF3FIFO:
            pipe_attributes.xdrEncode(xdr);
            break;
        default:
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        type = xdr.xdrDecodeInt();
        switch ( type ) {
        case ftype3.NF3CHR:
        case ftype3.NF3BLK:
            device = new devicedata3(xdr);
            break;
        case ftype3.NF3SOCK:
        case ftype3.NF3FIFO:
            pipe_attributes = new sattr3(xdr);
            break;
        default:
            break;
        }
    }

}
// End of mknoddata3.java
