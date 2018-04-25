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

public class createhow4 implements XdrAble {
    public int mode;
    public fattr4 createattrs;
    public verifier4 createverf;
    public creatverfattr ch_createboth;

    public createhow4() {
    }

    public createhow4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(mode);
        switch ( mode ) {
        case createmode4.UNCHECKED4:
        case createmode4.GUARDED4:
            createattrs.xdrEncode(xdr);
            break;
        case createmode4.EXCLUSIVE4:
            createverf.xdrEncode(xdr);
            break;
        case createmode4.EXCLUSIVE4_1:
            ch_createboth.xdrEncode(xdr);
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        mode = xdr.xdrDecodeInt();
        switch ( mode ) {
        case createmode4.UNCHECKED4:
        case createmode4.GUARDED4:
            createattrs = new fattr4(xdr);
            break;
        case createmode4.EXCLUSIVE4:
            createverf = new verifier4(xdr);
            break;
        case createmode4.EXCLUSIVE4_1:
            ch_createboth = new creatverfattr(xdr);
            break;
        }
    }

}
// End of createhow4.java
