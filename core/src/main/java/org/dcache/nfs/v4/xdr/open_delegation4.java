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

public class open_delegation4 implements XdrAble {
    public int delegation_type;
    public open_read_delegation4 read;
    public open_write_delegation4 write;
    public open_none_delegation4 od_whynone;

    public open_delegation4() {
    }

    public open_delegation4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(delegation_type);
        switch ( delegation_type ) {
        case open_delegation_type4.OPEN_DELEGATE_NONE:
            break;
        case open_delegation_type4.OPEN_DELEGATE_READ:
            read.xdrEncode(xdr);
            break;
        case open_delegation_type4.OPEN_DELEGATE_WRITE:
            write.xdrEncode(xdr);
            break;
        case open_delegation_type4.OPEN_DELEGATE_NONE_EXT:
            od_whynone.xdrEncode(xdr);
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        delegation_type = xdr.xdrDecodeInt();
        switch ( delegation_type ) {
        case open_delegation_type4.OPEN_DELEGATE_NONE:
            break;
        case open_delegation_type4.OPEN_DELEGATE_READ:
            read = new open_read_delegation4(xdr);
            break;
        case open_delegation_type4.OPEN_DELEGATE_WRITE:
            write = new open_write_delegation4(xdr);
            break;
        case open_delegation_type4.OPEN_DELEGATE_NONE_EXT:
            od_whynone = new open_none_delegation4(xdr);
            break;
        }
    }

}
// End of open_delegation4.java
