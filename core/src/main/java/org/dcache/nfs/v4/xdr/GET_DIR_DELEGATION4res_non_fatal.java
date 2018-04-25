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

public class GET_DIR_DELEGATION4res_non_fatal implements XdrAble {
    public int gddrnf_status;
    public GET_DIR_DELEGATION4resok gddrnf_resok4;
    public boolean gddrnf_will_signal_deleg_avail;

    public GET_DIR_DELEGATION4res_non_fatal() {
    }

    public GET_DIR_DELEGATION4res_non_fatal(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(gddrnf_status);
        switch ( gddrnf_status ) {
        case gddrnf4_status.GDD4_OK:
            gddrnf_resok4.xdrEncode(xdr);
            break;
        case gddrnf4_status.GDD4_UNAVAIL:
            xdr.xdrEncodeBoolean(gddrnf_will_signal_deleg_avail);
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        gddrnf_status = xdr.xdrDecodeInt();
        switch ( gddrnf_status ) {
        case gddrnf4_status.GDD4_OK:
            gddrnf_resok4 = new GET_DIR_DELEGATION4resok(xdr);
            break;
        case gddrnf4_status.GDD4_UNAVAIL:
            gddrnf_will_signal_deleg_avail = xdr.xdrDecodeBoolean();
            break;
        }
    }

}
// End of GET_DIR_DELEGATION4res_non_fatal.java
