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

import org.dcache.nfs.nfsstat;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class LAYOUTGET4res implements XdrAble {
    public int logr_status;
    public LAYOUTGET4resok logr_resok4;
    public boolean logr_will_signal_layout_avail;

    public LAYOUTGET4res() {
    }

    public LAYOUTGET4res(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(logr_status);
        switch (logr_status) {
            case nfsstat.NFS_OK:
                logr_resok4.xdrEncode(xdr);
                break;
            case nfsstat.NFSERR_LAYOUTTRYLATER:
                xdr.xdrEncodeBoolean(logr_will_signal_layout_avail);
                break;
            default:
                break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        logr_status = xdr.xdrDecodeInt();
        switch (logr_status) {
            case nfsstat.NFS_OK:
                logr_resok4 = new LAYOUTGET4resok(xdr);
                break;
            case nfsstat.NFSERR_LAYOUTTRYLATER:
                logr_will_signal_layout_avail = xdr.xdrDecodeBoolean();
                break;
            default:
                break;
        }
    }

}
// End of LAYOUTGET4res.java
