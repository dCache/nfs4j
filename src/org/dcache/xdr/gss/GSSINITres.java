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
 *
 * @author tigran
 */
public class GSSINITres implements  XdrAble {

    private byte[] handle;
    private int gssMajor;
    private int gssMinor;
    private int sequence;
    private byte[] token;

    public int getGssMajor() {
        return gssMajor;
    }

    public void setGssMajor(int gssMajor) {
        this.gssMajor = gssMajor;
    }

    public int getGssMinor() {
        return gssMinor;
    }

    public void setGssMinor(int gssMinor) {
        this.gssMinor = gssMinor;
    }

    public byte[] getHandle() {
        return handle;
    }

    public void setHandle(byte[] handle) {
        this.handle = handle;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        handle = xdr.xdrDecodeDynamicOpaque();
        gssMajor = xdr.xdrDecodeInt();
        gssMinor = xdr.xdrDecodeInt();
        sequence = xdr.xdrDecodeInt();
        token = xdr.xdrDecodeDynamicOpaque();
    }

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        xdr.xdrEncodeDynamicOpaque(handle);
        xdr.xdrEncodeInt(gssMajor);
        xdr.xdrEncodeInt(gssMinor);
        xdr.xdrEncodeInt(sequence);
        xdr.xdrEncodeDynamicOpaque(token);
    }

}
