/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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
package org.dcache.utils;

import org.dcache.chimera.posix.UnixUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class UnixUtils {
    private static final Logger _log = LoggerFactory.getLogger(UnixUtils.class);

    /**
     * attempts to get the current user, if running on a compatible OS/jre
     * @return the current UnixUser, or null
     */
    public static UnixUser getCurrentUser() {
        try {
            Class unixSystemClass = Class.forName("com.sun.security.auth.module.UnixSystem");
            Object unixSystemInstance = unixSystemClass.newInstance();
            Method getUidMethod = unixSystemClass.getDeclaredMethod("getUid");
            Method getGidMethod = unixSystemClass.getDeclaredMethod("getGid");
            Method getGroupsMethod = unixSystemClass.getDeclaredMethod("getGroups");
            Long uid = (Long) getUidMethod.invoke(unixSystemInstance);
            Long gid = (Long) getGidMethod.invoke(unixSystemInstance);
            long[] groups = (long[]) getGroupsMethod.invoke(unixSystemInstance);
            int[] groupInts = new int[groups.length];
            for (int i=0; i<groups.length; i++) {
                groupInts[i] = (int) groups[i];
            }
            return new UnixUser(uid.intValue(), gid.intValue(), groupInts);
        } catch (Exception e) {
            _log.debug("couldn't get current unix user",e);
            return null;
        }
    }
}
