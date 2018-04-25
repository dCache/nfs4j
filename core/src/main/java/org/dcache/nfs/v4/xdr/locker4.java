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

public class locker4 implements XdrAble {
    public boolean new_lock_owner;
    public open_to_lock_owner4 open_owner;
    public exist_lock_owner4 lock_owner;

    public locker4() {
    }

    public locker4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeBoolean(new_lock_owner);
        if ( new_lock_owner ) {
            open_owner.xdrEncode(xdr);
        }
        else if ( !new_lock_owner ) {
            lock_owner.xdrEncode(xdr);
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        new_lock_owner = xdr.xdrDecodeBoolean();
        if ( new_lock_owner ) {
            open_owner = new open_to_lock_owner4(xdr);
        }
        else if ( !new_lock_owner ) {
            lock_owner = new exist_lock_owner4(xdr);
        }
    }

}
// End of locker4.java
