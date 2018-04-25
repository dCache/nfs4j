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

public class GETDEVICELIST4args implements XdrAble {
    public int gdla_layout_type;
    public count4 gdla_maxdevices;
    public nfs_cookie4 gdla_cookie;
    public verifier4 gdla_cookieverf;

    public GETDEVICELIST4args() {
    }

    public GETDEVICELIST4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(gdla_layout_type);
        gdla_maxdevices.xdrEncode(xdr);
        gdla_cookie.xdrEncode(xdr);
        gdla_cookieverf.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        gdla_layout_type = xdr.xdrDecodeInt();
        gdla_maxdevices = new count4(xdr);
        gdla_cookie = new nfs_cookie4(xdr);
        gdla_cookieverf = new verifier4(xdr);
    }

}
// End of GETDEVICELIST4args.java
