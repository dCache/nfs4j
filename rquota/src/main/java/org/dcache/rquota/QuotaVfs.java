/*
 * Copyright (c) 2024 Deutsches Elektronen-Synchroton,
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
package org.dcache.rquota;

import org.dcache.rquota.xdr.rquota;

/**
 * Interface for querying quotas.
 */
public interface QuotaVfs {

    /**
     * User quota type.
     */
    int USER_QUOTA = 0;

    /**
     * Group quota type.
     */
    int GROUP_QUOTA = 1;

    /**
     * Get the quota for the given id.
     *
     * @param id   numeric id of user or group to get quota for.
     * @param type type of quota to get, either {@link #USER_QUOTA} or {@link #GROUP_QUOTA}.
     * @return the quota for the given subject
     */
    rquota getQuota(int id, int type);
}
