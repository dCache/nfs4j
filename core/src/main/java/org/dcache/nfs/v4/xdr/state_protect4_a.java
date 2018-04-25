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

public class state_protect4_a implements XdrAble {
    public int spa_how;
    public state_protect_ops4 spa_mach_ops;
    public ssv_sp_parms4 spa_ssv_parms;

    public state_protect4_a() {
    }

    public state_protect4_a(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(spa_how);
        switch ( spa_how ) {
        case state_protect_how4.SP4_NONE:
            break;
        case state_protect_how4.SP4_MACH_CRED:
            spa_mach_ops.xdrEncode(xdr);
            break;
        case state_protect_how4.SP4_SSV:
            spa_ssv_parms.xdrEncode(xdr);
            break;
        }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        spa_how = xdr.xdrDecodeInt();
        switch ( spa_how ) {
        case state_protect_how4.SP4_NONE:
            break;
        case state_protect_how4.SP4_MACH_CRED:
            spa_mach_ops = new state_protect_ops4(xdr);
            break;
        case state_protect_how4.SP4_SSV:
            spa_ssv_parms = new ssv_sp_parms4(xdr);
            break;
        }
    }

}
// End of state_protect4_a.java
