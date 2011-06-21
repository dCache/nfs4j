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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.dcache.chimera.nfs.v4.NfsLoginService;
import org.dcache.utils.Opaque;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

public class GssSessionManager {

    private static final Logger _log = Logger.getLogger(GssSessionManager.class.getName());
    private final GSSManager gManager = GSSManager.getInstance();
    private final GSSCredential _serviceCredential;
    private final NfsLoginService _loginService;

    public GssSessionManager(NfsLoginService loginService) throws GSSException {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        Oid krb5Mechanism = new Oid("1.2.840.113554.1.2.2");
        _serviceCredential = gManager.createCredential(null,
                GSSCredential.INDEFINITE_LIFETIME,
                krb5Mechanism, GSSCredential.ACCEPT_ONLY);
        _loginService = loginService;
    }
    private final Map<Opaque, GSSContext> sessions = new ConcurrentHashMap<Opaque, GSSContext>();

    public GSSContext createContext(byte[] handle) throws GSSException {
        GSSContext context = gManager.createContext(_serviceCredential);
        sessions.put(new Opaque(handle), context);
        return context;
    }

    public GSSContext getContext(byte[] handle) throws GSSException {
        GSSContext context = sessions.get(new Opaque(handle));
        if(context == null) {
            throw new GSSException(GSSException.NO_CONTEXT);
        }
        return context;
    }
    public GSSContext getEstablishedContext(byte[] handle) throws GSSException {
        GSSContext context = getContext(handle);
        if (!context.isEstablished()) {
            throw new GSSException(GSSException.NO_CONTEXT);
        }
        return context;
    }

    public GSSContext destroyContext(byte[] handle) throws GSSException {
        GSSContext context = sessions.remove(new Opaque(handle));
        if(!context.isEstablished()) {
            throw new GSSException(GSSException.NO_CONTEXT);
        }
        return context;
    }

    public Subject subjectOf(GSSName name) {
        return _loginService.login(  new KerberosPrincipal(name.toString()));
    }
}
