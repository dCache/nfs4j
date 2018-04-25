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
package org.dcache.nfs.v4.xdr;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;

public class createtype4 implements XdrAble {
    public int type;
    public linktext4 linkdata;
    public specdata4 devdata;

    public createtype4() {
    }

    public createtype4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(type);
        switch ( type ) {
        case nfs_ftype4.NF4LNK:
            linkdata.xdrEncode(xdr);
            break;
        case nfs_ftype4.NF4BLK:
        case nfs_ftype4.NF4CHR:
            devdata.xdrEncode(xdr);
            break;
        case nfs_ftype4.NF4SOCK:
        case nfs_ftype4.NF4FIFO:
        case nfs_ftype4.NF4DIR:
            break;
        default:
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        type = xdr.xdrDecodeInt();
        switch ( type ) {
        case nfs_ftype4.NF4LNK:
            linkdata = new linktext4(xdr);
            break;
        case nfs_ftype4.NF4BLK:
        case nfs_ftype4.NF4CHR:
            devdata = new specdata4(xdr);
            break;
        case nfs_ftype4.NF4SOCK:
        case nfs_ftype4.NF4FIFO:
        case nfs_ftype4.NF4DIR:
            break;
        default:
            break;
        }
    }

}
// End of createtype4.java
