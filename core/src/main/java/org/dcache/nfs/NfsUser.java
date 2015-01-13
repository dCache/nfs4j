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
package org.dcache.nfs;

import com.google.common.collect.Sets;
import java.util.Collections;
import javax.security.auth.Subject;
import org.dcache.auth.GidPrincipal;
import org.dcache.auth.UidPrincipal;

/**
 * Utility class extract user record from NFS request
 */
public class NfsUser {

    public final static int NOBODY = 65534;
    public final static Subject NFS_NOBODY = new Subject(true,
            Sets.newHashSet(
            new UidPrincipal(NfsUser.NOBODY),
            new GidPrincipal(NfsUser.NOBODY, true)),
            Collections.EMPTY_SET, Collections.EMPTY_SET);

    /*no instances allowed*/
    private NfsUser() {
    }

}
