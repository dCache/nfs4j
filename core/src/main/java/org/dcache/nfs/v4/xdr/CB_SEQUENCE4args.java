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

public class CB_SEQUENCE4args implements XdrAble {
    public sessionid4 csa_sessionid;
    public sequenceid4 csa_sequenceid;
    public slotid4 csa_slotid;
    public slotid4 csa_highest_slotid;
    public boolean csa_cachethis;
    public referring_call_list4 [] csa_referring_call_lists;

    public CB_SEQUENCE4args() {
    }

    public CB_SEQUENCE4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        csa_sessionid.xdrEncode(xdr);
        csa_sequenceid.xdrEncode(xdr);
        csa_slotid.xdrEncode(xdr);
        csa_highest_slotid.xdrEncode(xdr);
        xdr.xdrEncodeBoolean(csa_cachethis);
        { int $size = csa_referring_call_lists.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { csa_referring_call_lists[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        csa_sessionid = new sessionid4(xdr);
        csa_sequenceid = new sequenceid4(xdr);
        csa_slotid = new slotid4(xdr);
        csa_highest_slotid = new slotid4(xdr);
        csa_cachethis = xdr.xdrDecodeBoolean();
        { int $size = xdr.xdrDecodeInt(); csa_referring_call_lists = new referring_call_list4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { csa_referring_call_lists[$idx] = new referring_call_list4(xdr); } }
    }

}
// End of CB_SEQUENCE4args.java
