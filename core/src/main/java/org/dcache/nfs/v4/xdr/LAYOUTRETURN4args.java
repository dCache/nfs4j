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

public class LAYOUTRETURN4args implements XdrAble {
    public boolean lora_reclaim;
    public int lora_layout_type;
    public int lora_iomode;
    public layoutreturn4 lora_layoutreturn;

    public LAYOUTRETURN4args() {
    }

    public LAYOUTRETURN4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeBoolean(lora_reclaim);
        xdr.xdrEncodeInt(lora_layout_type);
        xdr.xdrEncodeInt(lora_iomode);
        lora_layoutreturn.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        lora_reclaim = xdr.xdrDecodeBoolean();
        lora_layout_type = xdr.xdrDecodeInt();
        lora_iomode = xdr.xdrDecodeInt();
        lora_layoutreturn = new layoutreturn4(xdr);
    }

}
// End of LAYOUTRETURN4args.java
