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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.dcache.utils.Opaque;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

public class GssSessionManager {

    private static final Logger _log = Logger.getLogger(GssSessionManager.class.getName());
    private final GSSManager gManager = GSSManager.getInstance();
    private final GSSCredential _serviceCredential;

    public GssSessionManager() throws GSSException {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        Oid krb5Mechanism = new Oid("1.2.840.113554.1.2.2");
        _serviceCredential = gManager.createCredential(null,
                GSSCredential.INDEFINITE_LIFETIME,
                krb5Mechanism, GSSCredential.ACCEPT_ONLY);
    }
    private final Map<Opaque, RpcGssContext> sessions = new ConcurrentHashMap<Opaque, RpcGssContext>();

    public RpcGssContext getCredential(RpcAuthGss gssAuth) throws GSSException {

        RpcGssContext cred;

        switch (gssAuth.getProc()) {
            case GssProc.RPCSEC_GSS_INIT:
                _log.fine("RPCSEC_GSS_INIT");
                UUID id = UUID.randomUUID();
                GSSContext context = gManager.createContext(_serviceCredential);
                cred = new RpcGssContext(id.toString().getBytes(), context);
                sessions.put(new Opaque(cred.getHandle()), cred);
                break;
            case GssProc.RPCSEC_GSS_DESTROY:
                _log.fine("RPCSEC_GSS_DESTROY");
                cred = sessions.remove(new Opaque(gssAuth.getHandle()));
                break;
            case GssProc.RPCSEC_GSS_CONTINUE_INIT:
                _log.fine("RPCSEC_GSS_CONTINUE_INIT");
                cred = sessions.get(new Opaque(gssAuth.getHandle()));
                break;
            case GssProc.RPCSEC_GSS_DATA:
                _log.fine("RPCSEC_GSS_DATA");
                cred = sessions.get(new Opaque(gssAuth.getHandle()));
                break;
            default:
                throw new RuntimeException("Invalid GssProc: " + gssAuth.getProc());
        }
        return cred;
    }
}
