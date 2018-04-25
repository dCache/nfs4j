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

public class entry3 implements XdrAble {
    public fileid3 fileid;
    public filename3 name;
    public cookie3 cookie;
    public entry3 nextentry;

    public entry3() {
    }

    public entry3(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        entry3 $this = this;
        do {
            $this.fileid.xdrEncode(xdr);
            $this.name.xdrEncode(xdr);
            $this.cookie.xdrEncode(xdr);
            $this = $this.nextentry;
            xdr.xdrEncodeBoolean($this != null);
        } while ( $this != null );
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        entry3 $this = this;
        entry3 $next;
        do {
            $this.fileid = new fileid3(xdr);
            $this.name = new filename3(xdr);
            $this.cookie = new cookie3(xdr);
            $next = xdr.xdrDecodeBoolean() ? new entry3() : null;
            $this.nextentry = $next;
            $this = $next;
        } while ( $this != null );
    }

}
// End of entry3.java
