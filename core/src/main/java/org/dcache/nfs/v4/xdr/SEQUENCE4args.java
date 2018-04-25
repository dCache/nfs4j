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

public class SEQUENCE4args implements XdrAble {
    public sessionid4 sa_sessionid;
    public sequenceid4 sa_sequenceid;
    public slotid4 sa_slotid;
    public slotid4 sa_highest_slotid;
    public boolean sa_cachethis;

    public SEQUENCE4args() {
    }

    public SEQUENCE4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        sa_sessionid.xdrEncode(xdr);
        sa_sequenceid.xdrEncode(xdr);
        sa_slotid.xdrEncode(xdr);
        sa_highest_slotid.xdrEncode(xdr);
        xdr.xdrEncodeBoolean(sa_cachethis);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        sa_sessionid = new sessionid4(xdr);
        sa_sequenceid = new sequenceid4(xdr);
        sa_slotid = new slotid4(xdr);
        sa_highest_slotid = new slotid4(xdr);
        sa_cachethis = xdr.xdrDecodeBoolean();
    }

}
// End of SEQUENCE4args.java
