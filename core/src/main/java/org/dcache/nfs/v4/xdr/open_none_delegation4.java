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

public class open_none_delegation4 implements XdrAble {
    public int ond_why;
    public boolean ond_server_will_push_deleg;
    public boolean ond_server_will_signal_avail;

    public open_none_delegation4() {
    }

    public open_none_delegation4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(ond_why);
        switch ( ond_why ) {
        case why_no_delegation4.WND4_CONTENTION:
            xdr.xdrEncodeBoolean(ond_server_will_push_deleg);
            break;
        case why_no_delegation4.WND4_RESOURCE:
            xdr.xdrEncodeBoolean(ond_server_will_signal_avail);
            break;
        default:
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        ond_why = xdr.xdrDecodeInt();
        switch ( ond_why ) {
        case why_no_delegation4.WND4_CONTENTION:
            ond_server_will_push_deleg = xdr.xdrDecodeBoolean();
            break;
        case why_no_delegation4.WND4_RESOURCE:
            ond_server_will_signal_avail = xdr.xdrDecodeBoolean();
            break;
        default:
            break;
        }
    }

}
// End of open_none_delegation4.java
