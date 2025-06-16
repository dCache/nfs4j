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

public class CB_NOTIFY4args implements XdrAble {
    public stateid4 cna_stateid;
    public nfs_fh4 cna_fh;
    public notify4[] cna_changes;

    public CB_NOTIFY4args() {
    }

    public CB_NOTIFY4args(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        cna_stateid.xdrEncode(xdr);
        cna_fh.xdrEncode(xdr);
        {
            int $size = cna_changes.length;
            xdr.xdrEncodeInt($size);
            for (int $idx = 0; $idx < $size; ++$idx) {
                cna_changes[$idx].xdrEncode(xdr);
            }
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        cna_stateid = new stateid4(xdr);
        cna_fh = new nfs_fh4(xdr);
        {
            int $size = xdr.xdrDecodeInt();
            cna_changes = new notify4[$size];
            for (int $idx = 0; $idx < $size; ++$idx) {
                cna_changes[$idx] = new notify4(xdr);
            }
        }
    }

}
// End of CB_NOTIFY4args.java
