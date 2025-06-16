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

public class callback_sec_parms4 implements XdrAble {
    public int cb_secflavor;
    public authsys_parms cbsp_sys_cred;
    public gss_cb_handles4 cbsp_gss_handles;

    public callback_sec_parms4() {
    }

    public callback_sec_parms4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(cb_secflavor);
        switch (cb_secflavor) {
            case nfs4_prot.AUTH_NONE:
                break;
            case nfs4_prot.AUTH_SYS:
                cbsp_sys_cred.xdrEncode(xdr);
                break;
            case nfs4_prot.RPCSEC_GSS:
                cbsp_gss_handles.xdrEncode(xdr);
                break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        cb_secflavor = xdr.xdrDecodeInt();
        switch (cb_secflavor) {
            case nfs4_prot.AUTH_NONE:
                break;
            case nfs4_prot.AUTH_SYS:
                cbsp_sys_cred = new authsys_parms(xdr);
                break;
            case nfs4_prot.RPCSEC_GSS:
                cbsp_gss_handles = new gss_cb_handles4(xdr);
                break;
        }
    }

}
// End of callback_sec_parms4.java
