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

public class channel_attrs4 implements XdrAble {
    public count4 ca_headerpadsize;
    public count4 ca_maxrequestsize;
    public count4 ca_maxresponsesize;
    public count4 ca_maxresponsesize_cached;
    public count4 ca_maxoperations;
    public count4 ca_maxrequests;
    public uint32_t [] ca_rdma_ird;

    public channel_attrs4() {
    }

    public channel_attrs4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        ca_headerpadsize.xdrEncode(xdr);
        ca_maxrequestsize.xdrEncode(xdr);
        ca_maxresponsesize.xdrEncode(xdr);
        ca_maxresponsesize_cached.xdrEncode(xdr);
        ca_maxoperations.xdrEncode(xdr);
        ca_maxrequests.xdrEncode(xdr);
        { int $size = ca_rdma_ird.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { ca_rdma_ird[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        ca_headerpadsize = new count4(xdr);
        ca_maxrequestsize = new count4(xdr);
        ca_maxresponsesize = new count4(xdr);
        ca_maxresponsesize_cached = new count4(xdr);
        ca_maxoperations = new count4(xdr);
        ca_maxrequests = new count4(xdr);
        { int $size = xdr.xdrDecodeInt(); ca_rdma_ird = new uint32_t[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { ca_rdma_ird[$idx] = new uint32_t(xdr); } }
    }

}
// End of channel_attrs4.java
