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

public class fs4_status implements XdrAble {
    public boolean fss_absent;
    public int fss_type;
    public utf8str_cs fss_source;
    public utf8str_cs fss_current;
    public int32_t fss_age;
    public nfstime4 fss_version;

    public fs4_status() {
    }

    public fs4_status(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeBoolean(fss_absent);
        xdr.xdrEncodeInt(fss_type);
        fss_source.xdrEncode(xdr);
        fss_current.xdrEncode(xdr);
        fss_age.xdrEncode(xdr);
        fss_version.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        fss_absent = xdr.xdrDecodeBoolean();
        fss_type = xdr.xdrDecodeInt();
        fss_source = new utf8str_cs(xdr);
        fss_current = new utf8str_cs(xdr);
        fss_age = new int32_t(xdr);
        fss_version = new nfstime4(xdr);
    }

}
// End of fs4_status.java
