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

public class notify_add4 implements XdrAble {
    public notify_remove4 [] nad_old_entry;
    public notify_entry4 nad_new_entry;
    public nfs_cookie4 [] nad_new_entry_cookie;
    public prev_entry4 [] nad_prev_entry;
    public boolean nad_last_entry;

    public notify_add4() {
    }

    public notify_add4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = nad_old_entry.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { nad_old_entry[$idx].xdrEncode(xdr); } }
        nad_new_entry.xdrEncode(xdr);
        { int $size = nad_new_entry_cookie.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { nad_new_entry_cookie[$idx].xdrEncode(xdr); } }
        { int $size = nad_prev_entry.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { nad_prev_entry[$idx].xdrEncode(xdr); } }
        xdr.xdrEncodeBoolean(nad_last_entry);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        { int $size = xdr.xdrDecodeInt(); nad_old_entry = new notify_remove4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { nad_old_entry[$idx] = new notify_remove4(xdr); } }
        nad_new_entry = new notify_entry4(xdr);
        { int $size = xdr.xdrDecodeInt(); nad_new_entry_cookie = new nfs_cookie4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { nad_new_entry_cookie[$idx] = new nfs_cookie4(xdr); } }
        { int $size = xdr.xdrDecodeInt(); nad_prev_entry = new prev_entry4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { nad_prev_entry[$idx] = new prev_entry4(xdr); } }
        nad_last_entry = xdr.xdrDecodeBoolean();
    }

}
// End of notify_add4.java
