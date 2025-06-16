/*
 * Copyright (c) 2020 - 2022 iterate GmbH
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

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.dcache.nfs.util.Cache;
import org.dcache.nfs.util.CacheElement;
import org.dcache.nfs.util.CacheEventListener;
import org.dcache.nfs.v4.xdr.clientid4;

public class DefaultClientCache extends Cache<clientid4, NFS4Client> implements ClientCache {
    public DefaultClientCache(Duration leaseTime, CacheEventListener<clientid4, NFS4Client> eventListener) {
        super("NFSv41 clients", 5000, Duration.ofSeconds(Long.MAX_VALUE),
                leaseTime.multipliedBy(2),
                eventListener);
    }

    @Override
    public Stream<NFS4Client> stream() {
        return entries().stream()
                .map(CacheElement::getObject);
    }

    @Override
    public Stream<NFS4Client> peek() {
        return entries().stream()
                .map(CacheElement::peekObject);
    }
}
