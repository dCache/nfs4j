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

import java.io.IOException;

import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class open_write_delegation4 implements XdrAble {
    public stateid4 stateid;
    public boolean recall;
    public nfs_space_limit4 space_limit;
    public nfsace4 permissions;

    public open_write_delegation4() {
    }

    public open_write_delegation4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        stateid.xdrEncode(xdr);
        xdr.xdrEncodeBoolean(recall);
        space_limit.xdrEncode(xdr);
        permissions.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        stateid = new stateid4(xdr);
        recall = xdr.xdrDecodeBoolean();
        space_limit = new nfs_space_limit4(xdr);
        permissions = new nfsace4(xdr);
    }

}
// End of open_write_delegation4.java
