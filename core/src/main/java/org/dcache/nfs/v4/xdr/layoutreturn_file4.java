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

public class layoutreturn_file4 implements XdrAble {
    public offset4 lrf_offset;
    public length4 lrf_length;
    public stateid4 lrf_stateid;
    public byte [] lrf_body;

    public layoutreturn_file4() {
    }

    public layoutreturn_file4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        lrf_offset.xdrEncode(xdr);
        lrf_length.xdrEncode(xdr);
        lrf_stateid.xdrEncode(xdr);
        xdr.xdrEncodeDynamicOpaque(lrf_body);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        lrf_offset = new offset4(xdr);
        lrf_length = new length4(xdr);
        lrf_stateid = new stateid4(xdr);
        lrf_body = xdr.xdrDecodeDynamicOpaque();
    }

}
// End of layoutreturn_file4.java
