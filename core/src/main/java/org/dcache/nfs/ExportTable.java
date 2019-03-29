/*
 * Copyright (c) 2019 Deutsches Elektronen-Synchroton,
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

import java.net.InetAddress;
import java.util.stream.Stream;

/**
 * NFS server export table.
 *
 * @since 0.19
 */
public interface ExportTable {

    /**
     * Get a {@link Stream} of all exports registered within this db.
     *
     * @return all registered export entries.
     */
    Stream<FsExport> exports();

    /**
     * Get a {@link Stream} of exports allowed for a given client.
     * The stream is ordered with most relevant export entry first.
     *
     * @param client address if the client.
     * @return all entries exported to a given client.
     */
    Stream<FsExport> exports(InetAddress client);

    /**
     * Get best matching export for a given path and client.
     *
     * @param path exported path
     * @param client address of the client.
     * @return best matching export entry for a given client or null if no
     * entries found.
     */
    FsExport getExport(String path, InetAddress client);

    /**
     * Get best matching export for a given path and client.
     *
     * @param index export index that identifies export path.
     * @param client address of the client.
     * @return best matching export entry for a given client or null if no
     * entries found.
     */
    FsExport getExport(int index, InetAddress client);
}
