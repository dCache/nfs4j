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
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.RpcAuthVerifier;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;

public class RpcAuthGss implements RpcAuth, XdrAble {

    private final static Logger _log = Logger.getLogger(RpcAuthGss.class.getName());

    private final int _type = RpcAuthType.RPCGSS_SEC;
    private RpcAuthVerifier _verifier = new RpcAuthVerifier(_type, new byte[0]);
    private int _version;
    private int _proc;
    private int _sequence;
    private int _service;
    private byte[] _handle;
    private Subject _subject = new Subject();

    public byte[] getHandle() {
        return _handle;
    }

    public void setHandle(byte[] handle) {
        _handle = handle;
    }

    public int getProc() {
        return _proc;
    }

    public void setProc(int proc) {
        _proc = proc;
    }

    public int getService() {
        return _service;
    }

    public void setService(int svc) {
        _service = svc;
    }

    public int getVersion() {
        return _version;
    }

    public void setVersion(int version) {
        _version = version;
    }

    @Override
    public Subject getSubject() {
        return _subject;
    }

    @Override
    public int type() {
        return _type;
    }

    @Override
    public RpcAuthVerifier getVerifier() {
        return _verifier;
    }

    public void setVerifier(RpcAuthVerifier verifier) {
        _verifier = verifier;
    }

    public int getSequence() {
        return _sequence;
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        int len = xdr.xdrDecodeInt();
        _version = xdr.xdrDecodeInt();
        _proc = xdr.xdrDecodeInt();
        _sequence = xdr.xdrDecodeInt();
        _service = xdr.xdrDecodeInt();
        _handle = xdr.xdrDecodeDynamicOpaque();
        _verifier.xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        xdr.xdrEncodeInt(_type);

        _verifier.xdrEncode(xdr);
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
