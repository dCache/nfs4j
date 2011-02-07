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
package org.dcache.chimera.nfs;

import org.dcache.chimera.posix.UnixUser;
import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.RpcAuthTypeUnix;
import org.dcache.xdr.RpcCall;

/**
 * Utility class extract user record from NFS request
 */
public class NfsUser {

    private final static int[] NO_GROUPS = new int[0];

    /*no instances allowed*/
    private NfsUser() {
    }

    public static UnixUser remoteUser(RpcCall call, ExportFile exports) {

        UnixUser user;
        int uid = -1;
        int gid = -1;
        int[] gids = NO_GROUPS;

        if (call.getCredential().type() == RpcAuthType.UNIX) {
            uid = ((RpcAuthTypeUnix) call.getCredential()).uid();
            gid = ((RpcAuthTypeUnix) call.getCredential()).gid();
            gids = ((RpcAuthTypeUnix) call.getCredential()).gids();
        }

        String host = call.getTransport().getRemoteSocketAddress().getAddress().getHostName();

        // root access only for trusted hosts
        if (uid == 0) {
            if ((exports == null) || !exports.isTrusted(
                    call.getTransport().getRemoteSocketAddress().getAddress())) {

                // FIXME: actual 'nobody' account should be used
                uid = -1;
                gid = -1;
            }
        }

        user = new UnixUser(uid, gid, gids, host);

        return user;

    }
}
