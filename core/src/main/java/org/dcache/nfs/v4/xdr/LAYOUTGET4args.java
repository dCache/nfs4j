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

public class LAYOUTGET4args implements XdrAble {
    public boolean loga_signal_layout_avail;
    public int loga_layout_type;
    public int loga_iomode;
    public offset4 loga_offset;
    public length4 loga_length;
    public length4 loga_minlength;
    public stateid4 loga_stateid;
    public count4 loga_maxcount;

    public LAYOUTGET4args() {
    }

    public LAYOUTGET4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeBoolean(loga_signal_layout_avail);
        xdr.xdrEncodeInt(loga_layout_type);
        xdr.xdrEncodeInt(loga_iomode);
        loga_offset.xdrEncode(xdr);
        loga_length.xdrEncode(xdr);
        loga_minlength.xdrEncode(xdr);
        loga_stateid.xdrEncode(xdr);
        loga_maxcount.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        loga_signal_layout_avail = xdr.xdrDecodeBoolean();
        loga_layout_type = xdr.xdrDecodeInt();
        loga_iomode = xdr.xdrDecodeInt();
        loga_offset = new offset4(xdr);
        loga_length = new length4(xdr);
        loga_minlength = new length4(xdr);
        loga_stateid = new stateid4(xdr);
        loga_maxcount = new count4(xdr);
    }

}
// End of LAYOUTGET4args.java
