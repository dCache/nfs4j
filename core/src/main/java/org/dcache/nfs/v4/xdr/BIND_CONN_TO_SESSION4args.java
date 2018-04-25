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

public class BIND_CONN_TO_SESSION4args implements XdrAble {
    public sessionid4 bctsa_sessid;
    public int bctsa_dir;
    public boolean bctsa_use_conn_in_rdma_mode;

    public BIND_CONN_TO_SESSION4args() {
    }

    public BIND_CONN_TO_SESSION4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        bctsa_sessid.xdrEncode(xdr);
        xdr.xdrEncodeInt(bctsa_dir);
        xdr.xdrEncodeBoolean(bctsa_use_conn_in_rdma_mode);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        bctsa_sessid = new sessionid4(xdr);
        bctsa_dir = xdr.xdrDecodeInt();
        bctsa_use_conn_in_rdma_mode = xdr.xdrDecodeBoolean();
    }

}
// End of BIND_CONN_TO_SESSION4args.java
