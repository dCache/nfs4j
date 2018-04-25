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
package org.dcache.nfs.v3.xdr;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;

public class PATHCONF3resok implements XdrAble {
    public post_op_attr obj_attributes;
    public uint32 linkmax;
    public uint32 name_max;
    public boolean no_trunc;
    public boolean chown_restricted;
    public boolean case_insensitive;
    public boolean case_preserving;

    public PATHCONF3resok() {
    }

    public PATHCONF3resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        obj_attributes.xdrEncode(xdr);
        linkmax.xdrEncode(xdr);
        name_max.xdrEncode(xdr);
        xdr.xdrEncodeBoolean(no_trunc);
        xdr.xdrEncodeBoolean(chown_restricted);
        xdr.xdrEncodeBoolean(case_insensitive);
        xdr.xdrEncodeBoolean(case_preserving);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        obj_attributes = new post_op_attr(xdr);
        linkmax = new uint32(xdr);
        name_max = new uint32(xdr);
        no_trunc = xdr.xdrDecodeBoolean();
        chown_restricted = xdr.xdrDecodeBoolean();
        case_insensitive = xdr.xdrDecodeBoolean();
        case_preserving = xdr.xdrDecodeBoolean();
    }

}
// End of PATHCONF3resok.java
