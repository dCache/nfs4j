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

public class GET_DIR_DELEGATION4resok implements XdrAble {
    public verifier4 gddr_cookieverf;
    public stateid4 gddr_stateid;
    public bitmap4 gddr_notification;
    public bitmap4 gddr_child_attributes;
    public bitmap4 gddr_dir_attributes;

    public GET_DIR_DELEGATION4resok() {
    }

    public GET_DIR_DELEGATION4resok(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        gddr_cookieverf.xdrEncode(xdr);
        gddr_stateid.xdrEncode(xdr);
        gddr_notification.xdrEncode(xdr);
        gddr_child_attributes.xdrEncode(xdr);
        gddr_dir_attributes.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        gddr_cookieverf = new verifier4(xdr);
        gddr_stateid = new stateid4(xdr);
        gddr_notification = new bitmap4(xdr);
        gddr_child_attributes = new bitmap4(xdr);
        gddr_dir_attributes = new bitmap4(xdr);
    }

}
// End of GET_DIR_DELEGATION4resok.java
