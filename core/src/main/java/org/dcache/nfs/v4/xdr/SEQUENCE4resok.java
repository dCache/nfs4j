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

public class SEQUENCE4resok implements XdrAble {
    public sessionid4 sr_sessionid;
    public sequenceid4 sr_sequenceid;
    public slotid4 sr_slotid;
    public slotid4 sr_highest_slotid;
    public slotid4 sr_target_highest_slotid;
    public uint32_t sr_status_flags;

    public SEQUENCE4resok() {
    }

    public SEQUENCE4resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        sr_sessionid.xdrEncode(xdr);
        sr_sequenceid.xdrEncode(xdr);
        sr_slotid.xdrEncode(xdr);
        sr_highest_slotid.xdrEncode(xdr);
        sr_target_highest_slotid.xdrEncode(xdr);
        sr_status_flags.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        sr_sessionid = new sessionid4(xdr);
        sr_sequenceid = new sequenceid4(xdr);
        sr_slotid = new slotid4(xdr);
        sr_highest_slotid = new slotid4(xdr);
        sr_target_highest_slotid = new slotid4(xdr);
        sr_status_flags = new uint32_t(xdr);
    }

}
// End of SEQUENCE4resok.java
