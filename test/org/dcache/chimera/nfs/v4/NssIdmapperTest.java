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
package org.dcache.chimera.nfs.v4;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class NssIdmapperTest {

    private final static String DOMAIN = "dcache.org";
    private static final String ROOT_UNAME = "root@" + DOMAIN;
    private static final String ROOT_GNAME = "root@" + DOMAIN;

    private static final int ROOT_UID = 0;
    private static final int ROOT_GID = 0;
    private NfsIdMapping _idMapping;

    @Before
    public void setUp() {
        _idMapping = new NssIdmapper(DOMAIN);
    }

    @Test
    public void testUidByName() {
        assertEquals(ROOT_UID, _idMapping.principalToUid(ROOT_UNAME));
    }

    @Test
    public void testUnameByUid() {
        assertEquals(ROOT_UNAME, _idMapping.uidToPrincipal(ROOT_UID));
    }
}
