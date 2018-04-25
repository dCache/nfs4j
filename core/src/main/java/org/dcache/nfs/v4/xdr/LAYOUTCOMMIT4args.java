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

public class LAYOUTCOMMIT4args implements XdrAble {
    public offset4 loca_offset;
    public length4 loca_length;
    public boolean loca_reclaim;
    public stateid4 loca_stateid;
    public newoffset4 loca_last_write_offset;
    public newtime4 loca_time_modify;
    public layoutupdate4 loca_layoutupdate;

    public LAYOUTCOMMIT4args() {
    }

    public LAYOUTCOMMIT4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        loca_offset.xdrEncode(xdr);
        loca_length.xdrEncode(xdr);
        xdr.xdrEncodeBoolean(loca_reclaim);
        loca_stateid.xdrEncode(xdr);
        loca_last_write_offset.xdrEncode(xdr);
        loca_time_modify.xdrEncode(xdr);
        loca_layoutupdate.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        loca_offset = new offset4(xdr);
        loca_length = new length4(xdr);
        loca_reclaim = xdr.xdrDecodeBoolean();
        loca_stateid = new stateid4(xdr);
        loca_last_write_offset = new newoffset4(xdr);
        loca_time_modify = new newtime4(xdr);
        loca_layoutupdate = new layoutupdate4(xdr);
    }

}
// End of LAYOUTCOMMIT4args.java
