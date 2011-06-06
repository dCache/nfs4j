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
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import java.util.logging.Level;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuthError;
import org.dcache.xdr.RpcAuthException;
import org.dcache.xdr.RpcAuthStat;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcRejectStatus;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrBuffer;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.ietf.jgss.GSSContext;

/**
 * An extention of {@link RpcCall} which Wrap/Unwrap the data according GSS QOS.
 * The supported QOS are: NONE, INTEGRITY and PRIVACY as specified in rfs 2203.
 *
 * @since 0.0.4
 */
public class RpcGssCall extends RpcCall {

    private final static Logger _log = Logger.getLogger(RpcGssCall.class.getName());
    private final GSSContext _gssContext;
    private final MessageProp _mop;

    public RpcGssCall(RpcCall call, GSSContext gssContext, MessageProp mop) {
        super(call.getXid(), call.getProgram(), call.getProgramVersion(),
                call.getProcedure(), call.getCredential(), call.getXdr(), call.getTransport());
        _gssContext = gssContext;
        _mop = mop;
    }

    @Override
    public void retrieveCall(XdrAble args) throws OncRpcException, IOException {
        try {
            RpcAuthGss authGss = (RpcAuthGss) getCredential();
            _log.log(Level.FINE, "Call with GSS service: {0}", authGss.getService());
            XdrDecodingStream xdr;
            switch (authGss.getService()) {
                case RpcGssService.RPC_GSS_SVC_NONE:
                    super.retrieveCall(args);
                    break;
                case RpcGssService.RPC_GSS_SVC_INTEGRITY:
                    DataBodyInteg integData = new DataBodyInteg();
                    super.retrieveCall(integData);
                    byte[] integBytes = integData.getData();
                    byte[] checksum = integData.getChecksum();
                    _gssContext.verifyMIC(checksum, 0, checksum.length,
                            integBytes, 0, integBytes.length, _mop);

                    xdr = new XdrBuffer(ByteBuffer.wrap(integBytes, 0, integBytes.length));
                    xdr.beginDecoding();
                    xdr.xdrDecodeInt(); // first 4 bytes of data is the sequence number. Skip it.
                    args.xdrDecode(xdr);
                    xdr.endDecoding();
                    break;
                case RpcGssService.RPC_GSS_SVC_PRIVACY:
                    DataBodyPrivacy privacyData = new DataBodyPrivacy();
                    super.retrieveCall(privacyData);
                    byte[] privacyBytes = privacyData.getData();
                    byte[] rawData = _gssContext.unwrap(privacyBytes, 0, privacyBytes.length, _mop);

                    xdr = new XdrBuffer(ByteBuffer.wrap(rawData, 0, rawData.length));
                    xdr.beginDecoding();
                    xdr.xdrDecodeInt(); // first 4 bytes of data is the sequence number. Skip it.
                    args.xdrDecode(xdr);
                    xdr.endDecoding();
            }
        } catch (GSSException e) {
            _log.log(Level.SEVERE, "GSS error: {0}", e.getMessage());
            throw new RpcAuthException( "GSS error: " + e.getMessage() ,
                    new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
        }
    }

    @Override
    public void acceptedReply(int state, XdrAble reply) {
        try {
            RpcAuthGss authGss = (RpcAuthGss) getCredential();
            _log.log(Level.FINE, "Reply with GSS service: {0}", authGss.getService());
            XdrEncodingStream xdr;
            switch (authGss.getService()) {
                case RpcGssService.RPC_GSS_SVC_NONE:
                    super.acceptedReply(state, reply);
                    break;
                case RpcGssService.RPC_GSS_SVC_INTEGRITY:
                    xdr = new XdrBuffer(256 * 1024);
                    xdr.beginEncoding();
                    xdr.xdrEncodeInt(authGss.getSequence());
                    reply.xdrEncode(xdr);
                    xdr.endEncoding();
                    ByteBuffer b = xdr.body();
                    byte[] integBytes = new byte[b.limit()];
                    b.get(integBytes);

                    byte[] checksum = _gssContext.getMIC(integBytes, 0, integBytes.length, _mop);
                    DataBodyInteg integData = new DataBodyInteg(integBytes, checksum);
                    super.acceptedReply(state, integData);
                    break;
                case RpcGssService.RPC_GSS_SVC_PRIVACY:
                    xdr = new XdrBuffer(256 * 1024);
                    xdr.beginEncoding();
                    xdr.xdrEncodeInt(authGss.getSequence());
                    reply.xdrEncode(xdr);
                    xdr.endEncoding();
                    ByteBuffer bp = xdr.body();
                    byte[] rawData = new byte[bp.limit()];
                    bp.get(rawData);

                    byte[] privacyBytes = _gssContext.wrap(rawData, 0, rawData.length, _mop);
                    DataBodyPrivacy privacyData = new DataBodyPrivacy(privacyBytes);
                    super.acceptedReply(state, privacyData);
                    break;
            }

        } catch (IOException e) {
            _log.log(Level.SEVERE, "IO error: {0}", e.getMessage());
            super.reject(RpcRejectStatus.AUTH_ERROR, new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
        } catch (OncRpcException e) {
            _log.log(Level.SEVERE, "RPC error: {0}", e.getMessage());
            super.reject(RpcRejectStatus.AUTH_ERROR, new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
        } catch (GSSException e) {
            _log.log(Level.SEVERE, "GSS error: {0}", e.getMessage());
            super.reject(RpcRejectStatus.AUTH_ERROR, new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
        }
    }
}
