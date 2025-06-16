/*
 * Copyright (c) 2020 iterate GmbH
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

import java.util.stream.Stream;

import org.dcache.nfs.v4.xdr.clientid4;

public interface ClientCache {

    /**
     * Cache client by id
     *
     * @param clientid4 Client id
     * @param nfs4Client Client reference
     */
    void put(clientid4 clientid4, NFS4Client nfs4Client);

    /**
     * @param clientid4 Client id
     * @return Cached client or null if not found
     */
    NFS4Client get(clientid4 clientid4);

    /**
     * Remove client by id
     *
     * @param clientid4 Client id
     * @return Previously cached client if any
     */
    NFS4Client remove(clientid4 clientid4);

    /**
     * Check and remove expired entries.
     */
    void cleanUp();

    /**
     * @return Return cached clients and update last access time for elements retrieved.
     */
    Stream<NFS4Client> stream();

    /**
     * @return Return cached clients. The last access time of the element will not be updated.
     */
    Stream<NFS4Client> peek();
}
