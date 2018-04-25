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

public class CREATE_SESSION4resok implements XdrAble {
    public sessionid4 csr_sessionid;
    public sequenceid4 csr_sequence;
    public uint32_t csr_flags;
    public channel_attrs4 csr_fore_chan_attrs;
    public channel_attrs4 csr_back_chan_attrs;

    public CREATE_SESSION4resok() {
    }

    public CREATE_SESSION4resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        csr_sessionid.xdrEncode(xdr);
        csr_sequence.xdrEncode(xdr);
        csr_flags.xdrEncode(xdr);
        csr_fore_chan_attrs.xdrEncode(xdr);
        csr_back_chan_attrs.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        csr_sessionid = new sessionid4(xdr);
        csr_sequence = new sequenceid4(xdr);
        csr_flags = new uint32_t(xdr);
        csr_fore_chan_attrs = new channel_attrs4(xdr);
        csr_back_chan_attrs = new channel_attrs4(xdr);
    }

}
// End of CREATE_SESSION4resok.java
