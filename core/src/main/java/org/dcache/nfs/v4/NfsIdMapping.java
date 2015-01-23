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

import org.dcache.nfs.status.BadOwnerException;
/**
 * libnfsidmap like interface for {@link String} to uid/gid mapping.
 *
 * See http://www.citi.umich.edu/projects/nfsv4/linux/ for details.
 *
 * @since 0.0.4
 */
public interface NfsIdMapping {

    /**
     * Get numeric uid for principal. If no valid mapping found, uid of NOBODY is returned.
     *
     * @param principal to map
     * @return numeric id;
     * @throws BadOwnerException if principal cannot be translated into local numeric id.
     */
    int principalToUid(String principal) throws BadOwnerException;

    /**
     * Get numeric gid for principal. If no valid mapping found, gid of NOBODY is returned.
     *
     * @param principal to map
     * @return numeric id
     * @throws BadOwnerException if principal cannot be translated into local numeric id.
     */
    int principalToGid(String principal) throws BadOwnerException;

    /**
     * Get {@link String} corresponding to provided numeric uid. If there is no valid
     * mapping from uid to name, then the numerical string representing uid
     * is returned instead.
     * @param id to map
     * @return principal
     */
    String uidToPrincipal(int id);

    /**
     * Get {@link String} corresponding to provided numeric gid. If there is no valid
     * mapping from gid to name, then the numerical string representing gid
     * is returned instead.
     * @param id to map
     * @return principal
     */
    String gidToPrincipal(int id);
}
