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

public class deleg_claim4 implements XdrAble {
    public int dc_claim;
    public int dc_delegate_type;

    public deleg_claim4() {
    }

    public deleg_claim4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(dc_claim);
        switch ( dc_claim ) {
        case open_claim_type4.CLAIM_FH:
            break;
        case open_claim_type4.CLAIM_DELEG_PREV_FH:
            break;
        case open_claim_type4.CLAIM_PREVIOUS:
            xdr.xdrEncodeInt(dc_delegate_type);
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        dc_claim = xdr.xdrDecodeInt();
        switch ( dc_claim ) {
        case open_claim_type4.CLAIM_FH:
            break;
        case open_claim_type4.CLAIM_DELEG_PREV_FH:
            break;
        case open_claim_type4.CLAIM_PREVIOUS:
            dc_delegate_type = xdr.xdrDecodeInt();
            break;
        }
    }

}
// End of deleg_claim4.java
