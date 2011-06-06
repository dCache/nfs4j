/*
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
package org.dcache.xdr.gss;

import java.io.IOException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;

/**
 * The data for a GSS context creation request.
 */
public class GSSINITargs implements XdrAble {

    private byte[] token;

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public GSSINITargs() {
    }

    public GSSINITargs(byte[] token) {
        this.token = token;
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        token = xdr.xdrDecodeDynamicOpaque();
    }

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        xdr.xdrEncodeDynamicOpaque(token);
    }

}
