/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4;

import java.security.Principal;
import javax.security.auth.Subject;
import org.dcache.auth.Subjects;

/**
 * Simple implementation of {@link NfsIdMapping} which converts number into
 * string representation and vice versa.
 *
 * @since 0.0.4
 */
public class SimpleIdMap implements NfsIdMapping, NfsLoginService {

    private static final int NOBODY_UID = 65534;
    private static final int NOBODY_GID = 65534;

    private static final int DEFAULT_UID = 1001;
    private static final int DEFAULT_GID = 1001;

    @Override
    public int principalToGid(String principal) {
        try {
            return Integer.parseInt(principal);
        } catch (NumberFormatException e) {
        }
        return NOBODY_GID;
    }

    @Override
    public int principalToUid(String principal) {
        try {
            return Integer.parseInt(principal);
        } catch (NumberFormatException e) {
        }
        return NOBODY_UID;
    }

    @Override
    public String uidToPrincipal(int id) {
        return Integer.toString(id);
    }

    @Override
    public String gidToPrincipal(int id) {
        return Integer.toString(id);
    }

    @Override
    public Subject login(Principal principal) {
        return Subjects.of(DEFAULT_UID, DEFAULT_GID);
    }
}
