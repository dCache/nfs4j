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

public class CB_LAYOUTRECALL4args implements XdrAble {
    public int clora_type;
    public int clora_iomode;
    public boolean clora_changed;
    public layoutrecall4 clora_recall;

    public CB_LAYOUTRECALL4args() {
    }

    public CB_LAYOUTRECALL4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(clora_type);
        xdr.xdrEncodeInt(clora_iomode);
        xdr.xdrEncodeBoolean(clora_changed);
        clora_recall.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        clora_type = xdr.xdrDecodeInt();
        clora_iomode = xdr.xdrDecodeInt();
        clora_changed = xdr.xdrDecodeBoolean();
        clora_recall = new layoutrecall4(xdr);
    }

}
// End of CB_LAYOUTRECALL4args.java
