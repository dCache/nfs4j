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

import java.io.IOException;

import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class SYMLINK3resok implements XdrAble {
    public post_op_fh3 obj;
    public post_op_attr obj_attributes;
    public wcc_data dir_wcc;

    public SYMLINK3resok() {
    }

    public SYMLINK3resok(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        obj.xdrEncode(xdr);
        obj_attributes.xdrEncode(xdr);
        dir_wcc.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        obj = new post_op_fh3(xdr);
        obj_attributes = new post_op_attr(xdr);
        dir_wcc = new wcc_data(xdr);
    }

}
// End of SYMLINK3resok.java
