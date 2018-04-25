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

public class CB_COMPOUND4args implements XdrAble {
    public utf8str_cs tag;
    public uint32_t minorversion;
    public uint32_t callback_ident;
    public nfs_cb_argop4 [] argarray;

    public CB_COMPOUND4args() {
    }

    public CB_COMPOUND4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        tag.xdrEncode(xdr);
        minorversion.xdrEncode(xdr);
        callback_ident.xdrEncode(xdr);
        { int $size = argarray.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { argarray[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        tag = new utf8str_cs(xdr);
        minorversion = new uint32_t(xdr);
        callback_ident = new uint32_t(xdr);
        { int $size = xdr.xdrDecodeInt(); argarray = new nfs_cb_argop4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { argarray[$idx] = new nfs_cb_argop4(xdr); } }
    }

}
// End of CB_COMPOUND4args.java
