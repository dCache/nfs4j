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

import java.io.IOException;

import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class open_claim4 implements XdrAble {
    public int claim;
    public component4 file;
    public int delegate_type;
    public open_claim_delegate_cur4 delegate_cur_info;
    public component4 file_delegate_prev;
    public stateid4 oc_delegate_stateid;

    public open_claim4() {
    }

    public open_claim4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(claim);
        switch (claim) {
            case open_claim_type4.CLAIM_NULL:
                file.xdrEncode(xdr);
                break;
            case open_claim_type4.CLAIM_PREVIOUS:
                xdr.xdrEncodeInt(delegate_type);
                break;
            case open_claim_type4.CLAIM_DELEGATE_CUR:
                delegate_cur_info.xdrEncode(xdr);
                break;
            case open_claim_type4.CLAIM_DELEGATE_PREV:
                file_delegate_prev.xdrEncode(xdr);
                break;
            case open_claim_type4.CLAIM_FH:
                break;
            case open_claim_type4.CLAIM_DELEG_PREV_FH:
                break;
            case open_claim_type4.CLAIM_DELEG_CUR_FH:
                oc_delegate_stateid.xdrEncode(xdr);
                break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        claim = xdr.xdrDecodeInt();
        switch (claim) {
            case open_claim_type4.CLAIM_NULL:
                file = new component4(xdr);
                break;
            case open_claim_type4.CLAIM_PREVIOUS:
                delegate_type = xdr.xdrDecodeInt();
                break;
            case open_claim_type4.CLAIM_DELEGATE_CUR:
                delegate_cur_info = new open_claim_delegate_cur4(xdr);
                break;
            case open_claim_type4.CLAIM_DELEGATE_PREV:
                file_delegate_prev = new component4(xdr);
                break;
            case open_claim_type4.CLAIM_FH:
                break;
            case open_claim_type4.CLAIM_DELEG_PREV_FH:
                break;
            case open_claim_type4.CLAIM_DELEG_CUR_FH:
                oc_delegate_stateid = new stateid4(xdr);
                break;
        }
    }

}
// End of open_claim4.java
