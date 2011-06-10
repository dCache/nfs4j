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

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.google.common.primitives.Ints;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuthError;
import org.dcache.xdr.RpcAuthException;
import org.dcache.xdr.RpcAuthStat;
import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.RpcAuthVerifier;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcException;
import org.dcache.xdr.RpcProtocolFilter;
import org.dcache.xdr.RpcRejectStatus;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;

/**
 * A {@link ProtocolFilter} that handles RPCSEC_GSS requests.
 * Filter is responsible to establish and destroy GSS context.
 * For requests with established contexts RPC requests repacked into
 * GSS aware {@link RpsGssCall}.
 *
 * @since 0.0.4
 */
public class GssProtocolFilter implements ProtocolFilter {

    private final static Logger _log = Logger.getLogger(GssProtocolFilter.class.getName());
    /**
     * Return value from either accept or init stating that
     * the context creation phase is complete for this peer.
     * @see #init
     * @see #accept
     */
    public static final int COMPLETE = 0;
    /**
     * Return value from either accept or init stating that
     * another token is required from the peer to continue context
     * creation. This may be returned several times indicating
     * multiple token exchanges.
     * @see #init
     * @see #accept
     */
    public static final int CONTINUE_NEEDED = 1;

    private final GssSessionManager _gssSessionManager;

    public GssProtocolFilter(GssSessionManager gssSessionManager) {
        _gssSessionManager = gssSessionManager;
    }

    @Override
    public boolean execute(Context context) throws IOException {

        RpcCall call = (RpcCall) context.getAttribute(RpcProtocolFilter.RPC_CALL);

        if (call.getCredential().type() != RpcAuthType.RPCGSS_SEC) {
            return true;
        }

        boolean hasContext = false;
        try {
            RpcAuthGss authGss = (RpcAuthGss) call.getCredential();
            RpcGssContext cred = _gssSessionManager.getCredential(authGss);
            if (cred == null) {
                throw new RpcAuthException("No context found",
                        new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
            }

            GSSContext gssContext = cred.getContext();
            int _sequence = authGss.getSequence();
            switch (authGss.getProc()) {
                case GssProc.RPCSEC_GSS_INIT:
                case GssProc.RPCSEC_GSS_CONTINUE_INIT:

                    GSSINITargs gssArgs = new GSSINITargs();
                    GSSINITres res = new GSSINITres();
                    call.retrieveCall(gssArgs);
                    byte[] inToken = gssArgs.getToken();
                    byte[] outToken = gssContext.acceptSecContext(inToken, 0, inToken.length);
                    res.setHandle(cred.getHandle());
                    res.setGssMajor(cred.getContext().isEstablished() ? COMPLETE : CONTINUE_NEEDED);
                    res.setGssMinor(0);
                    res.setToken(outToken);
                    if (gssContext.isEstablished()) {
                        // FIXME: hard coded number
                        _sequence = 2;
                        res.setSequence(_sequence);
                        byte[] crc = Ints.toByteArray(_sequence);
                        crc = gssContext.getMIC(crc, 0, 4, new MessageProp(false));
                        authGss.setVerifier(new RpcAuthVerifier(authGss.type(), crc));
                    }
                    call.reply(res);
                    break;
                case GssProc.RPCSEC_GSS_DESTROY:
                    cred.getContext().dispose();
                    break;
                case GssProc.RPCSEC_GSS_DATA:
                    _log.log(Level.FINE, "RPCGSS_SEC: {0}",gssContext.getSrcName());
                    byte[] crc = Ints.toByteArray(authGss.getSequence());
                    crc = gssContext.getMIC(crc, 0, 4, new MessageProp(false));
                    authGss.setVerifier(new RpcAuthVerifier(authGss.type(), crc));
                    context.setAttribute(RpcProtocolFilter.RPC_CALL,
                            new RpcGssCall(call, cred.getContext(), new MessageProp(false)));
                    hasContext = true;
            }

        } catch (RpcException e) {
            call.reject(e.getStatus(), e.getRpcReply());
            _log.log(Level.INFO, "GSS mechanism failed {0}", e.getMessage());
        } catch (IOException e) {
            call.reject(RpcRejectStatus.AUTH_ERROR, new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
            _log.log(Level.INFO, "GSS mechanism failed {0}", e.getMessage());
        } catch (OncRpcException e) {
            call.reject(RpcRejectStatus.AUTH_ERROR, new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
            _log.log(Level.INFO, "RPC request rejected: {0}", e.getMessage());
        } catch (GSSException e) {
            call.reject(RpcRejectStatus.AUTH_ERROR, new RpcAuthError(RpcAuthStat.RPCSEC_GSS_CTXPROBLEM));
            _log.log(Level.INFO, "GSS mechanism failed {0}", e.getMessage());
        }
        return hasContext;
    }

    @Override
    public boolean postExecute(Context cntxt) throws IOException {
        return true;
    }
}
