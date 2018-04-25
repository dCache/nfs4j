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

public class nfsace4 implements XdrAble {
    public acetype4 type;
    public aceflag4 flag;
    public acemask4 access_mask;
    public utf8str_mixed who;

    public nfsace4() {
    }

    public nfsace4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        type.xdrEncode(xdr);
        flag.xdrEncode(xdr);
        access_mask.xdrEncode(xdr);
        who.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        type = new acetype4(xdr);
        flag = new aceflag4(xdr);
        access_mask = new acemask4(xdr);
        who = new utf8str_mixed(xdr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (type.value.value) {
            case nfs4_prot.ACE4_ACCESS_ALLOWED_ACE_TYPE:
                sb.append('A');
                break;
            case nfs4_prot.ACE4_ACCESS_DENIED_ACE_TYPE:
                sb.append('D');
                break;
            case nfs4_prot.ACE4_SYSTEM_ALARM_ACE_TYPE:
                sb.append('L');
                break;
            case nfs4_prot.ACE4_SYSTEM_AUDIT_ACE_TYPE:
                sb.append('U');
                break;
            default:
                sb.append("X");
        }

        sb.append(':');
        // flag
        sb.append(':');
        sb.append(who);
        sb.append(':');
        sb.append(acemask4.toString(access_mask.value.value));
        return sb.toString();
    }
}
// End of nfsace4.java
