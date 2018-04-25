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

public class GETDEVICEINFO4args implements XdrAble {
    public deviceid4 gdia_device_id;
    public int gdia_layout_type;
    public count4 gdia_maxcount;
    public bitmap4 gdia_notify_types;

    public GETDEVICEINFO4args() {
    }

    public GETDEVICEINFO4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        gdia_device_id.xdrEncode(xdr);
        xdr.xdrEncodeInt(gdia_layout_type);
        gdia_maxcount.xdrEncode(xdr);
        gdia_notify_types.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        gdia_device_id = new deviceid4(xdr);
        gdia_layout_type = xdr.xdrDecodeInt();
        gdia_maxcount = new count4(xdr);
        gdia_notify_types = new bitmap4(xdr);
    }

}
// End of GETDEVICEINFO4args.java
