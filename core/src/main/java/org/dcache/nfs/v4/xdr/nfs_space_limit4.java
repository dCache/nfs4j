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

public class nfs_space_limit4 implements XdrAble {
    public int limitby;
    public uint64_t filesize;
    public nfs_modified_limit4 mod_blocks;

    public nfs_space_limit4() {
    }

    public nfs_space_limit4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(limitby);
        switch ( limitby ) {
        case limit_by4.NFS_LIMIT_SIZE:
            filesize.xdrEncode(xdr);
            break;
        case limit_by4.NFS_LIMIT_BLOCKS:
            mod_blocks.xdrEncode(xdr);
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        limitby = xdr.xdrDecodeInt();
        switch ( limitby ) {
        case limit_by4.NFS_LIMIT_SIZE:
            filesize = new uint64_t(xdr);
            break;
        case limit_by4.NFS_LIMIT_BLOCKS:
            mod_blocks = new nfs_modified_limit4(xdr);
            break;
        }
    }

}
// End of nfs_space_limit4.java
