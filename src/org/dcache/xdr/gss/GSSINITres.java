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

    private byte[] _handle;
    private int _gssMajor;
    private int _gssMinor;
    private int _sequence;
    private byte[] _token;

    public int getGssMajor() {
        return _gssMajor;
    }

    public void setGssMajor(int gssMajor) {
        this._gssMajor = gssMajor;
    }

    public int getGssMinor() {
        return _gssMinor;
    }

    public void setGssMinor(int gssMinor) {
        this._gssMinor = gssMinor;
    }

    public byte[] getHandle() {
        return _handle;
    }

    public void setHandle(byte[] handle) {
        this._handle = handle;
    }

    public int getSequence() {
        return _sequence;
    }

    public void setSequence(int sequence) {
        this._sequence = sequence;
    }

    public byte[] getToken() {
        return _token;
    }

    public void setToken(byte[] token) {
        this._token = token;
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        _handle = xdr.xdrDecodeDynamicOpaque();
        _gssMajor = xdr.xdrDecodeInt();
        _gssMinor = xdr.xdrDecodeInt();
        _sequence = xdr.xdrDecodeInt();
        _token = xdr.xdrDecodeDynamicOpaque();
    }

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        xdr.xdrEncodeDynamicOpaque(_handle);
        xdr.xdrEncodeInt(_gssMajor);
        xdr.xdrEncodeInt(_gssMinor);
        xdr.xdrEncodeInt(_sequence);
        xdr.xdrEncodeDynamicOpaque(_token);
    }

}
