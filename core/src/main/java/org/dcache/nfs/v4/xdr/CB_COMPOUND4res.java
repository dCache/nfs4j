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

public class CB_COMPOUND4res implements XdrAble {
    public int status;
    public utf8str_cs tag;
    public nfs_cb_resop4[] resarray;

    public CB_COMPOUND4res() {
    }

    public CB_COMPOUND4res(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(status);
        tag.xdrEncode(xdr);
        {
            int $size = resarray.length;
            xdr.xdrEncodeInt($size);
            for (int $idx = 0; $idx < $size; ++$idx) {
                resarray[$idx].xdrEncode(xdr);
            }
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        status = xdr.xdrDecodeInt();
        tag = new utf8str_cs(xdr);
        {
            int $size = xdr.xdrDecodeInt();
            resarray = new nfs_cb_resop4[$size];
            for (int $idx = 0; $idx < $size; ++$idx) {
                resarray[$idx] = new nfs_cb_resop4(xdr);
            }
        }
    }

}
// End of CB_COMPOUND4res.java
