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

public class GETDEVICELIST4resok implements XdrAble {
    public nfs_cookie4 gdlr_cookie;
    public verifier4 gdlr_cookieverf;
    public deviceid4 [] gdlr_deviceid_list;
    public boolean gdlr_eof;

    public GETDEVICELIST4resok() {
    }

    public GETDEVICELIST4resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        gdlr_cookie.xdrEncode(xdr);
        gdlr_cookieverf.xdrEncode(xdr);
        { int $size = gdlr_deviceid_list.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { gdlr_deviceid_list[$idx].xdrEncode(xdr); } }
        xdr.xdrEncodeBoolean(gdlr_eof);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        gdlr_cookie = new nfs_cookie4(xdr);
        gdlr_cookieverf = new verifier4(xdr);
        { int $size = xdr.xdrDecodeInt(); gdlr_deviceid_list = new deviceid4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { gdlr_deviceid_list[$idx] = new deviceid4(xdr); } }
        gdlr_eof = xdr.xdrDecodeBoolean();
    }

}
// End of GETDEVICELIST4resok.java
