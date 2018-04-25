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

public class OPEN4args implements XdrAble {
    public seqid4 seqid;
    public uint32_t share_access;
    public uint32_t share_deny;
    public open_owner4 owner;
    public openflag4 openhow;
    public open_claim4 claim;

    public OPEN4args() {
    }

    public OPEN4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        seqid.xdrEncode(xdr);
        share_access.xdrEncode(xdr);
        share_deny.xdrEncode(xdr);
        owner.xdrEncode(xdr);
        openhow.xdrEncode(xdr);
        claim.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        seqid = new seqid4(xdr);
        share_access = new uint32_t(xdr);
        share_deny = new uint32_t(xdr);
        owner = new open_owner4(xdr);
        openhow = new openflag4(xdr);
        claim = new open_claim4(xdr);
    }

}
// End of OPEN4args.java
