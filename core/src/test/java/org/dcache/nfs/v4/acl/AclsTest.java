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
package org.dcache.nfs.v4.acl;

import org.dcache.nfs.v4.acl.Acls;
import org.dcache.nfs.v4.xdr.acemask4;
import org.dcache.nfs.v4.xdr.utf8str_mixed;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.v4.xdr.acetype4;
import org.dcache.nfs.v4.xdr.aceflag4;
import org.dcache.nfs.v4.xdr.uint32_t;
import static org.dcache.nfs.v4.xdr.nfs4_prot.*;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.dcache.nfs.v4.acl.Acls.*;
import org.junit.Ignore;

public class AclsTest {

    @Test
    public void testBackForward() {
        int rwx = 0755;
        nfsace4[] acl = of(rwx, true);
        assertEquals(rwx, toMode(acl));
    }

    @Test
    public void testOwnerDenyWrite() {
        int rwx = 0755;
        int expected = 0555;
        nfsace4[] acl = of(rwx, true);

        nfsace4 deny = toACE(Acls.OWNER, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_APPEND_DATA);
        nfsace4[] acl2 = addACE(deny, acl);
        assertEquals(expected, toMode(acl2));
    }

    @Test
    public void testOwnerDenyWriteIgnore() {
        int rwx = 0755;
        nfsace4[] acl = of(rwx, true);

        nfsace4 deny = toACE(Acls.OWNER, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_APPEND_DATA);
        nfsace4[] acl2 = addACE(acl, deny);
        assertEquals(rwx, toMode(acl2));
    }

    @Test
    public void testAllDenyRead() {
        int rwx = 0755;
        int expected = 0311;
        nfsace4[] acl = of(rwx, true);

        nfsace4 deny = toACE(Acls.EVERYONE, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_READ_DATA);
        nfsace4[] acl2 = addACE(deny, acl);
        assertEquals(expected, toMode(acl2));
    }

    @Test
    public void testAllRead() {
        int rwx = 0444;

        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.EVERYONE, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA)
        };

        assertEquals(rwx, toMode(acl));
    }

    private nfsace4 toACE(utf8str_mixed principal, int type, int mask) {
        nfsace4 ace = new nfsace4();
        ace.who = principal;
        ace.access_mask = new acemask4(new uint32_t(mask));
        ace.type = new acetype4(new uint32_t(type));
        int flags = principal == Acls.GROUP ? ACE4_IDENTIFIER_GROUP : 0;
        ace.flag = new aceflag4(new uint32_t(flags));
        return ace;
    }

    @Ignore
    @Test
    public void testUpdateUnixMode() {
        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.GROUP, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_WRITE_ACL),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_WRITE_RETENTION_HOLD),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_DELETE)
        };
        int mode = 0755;
        assertMode(mode, toMode(adjust(acl, mode)));
    }

    @Test
    public void testCompactByPrincipal() {
        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.GROUP, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_WRITE_ACL),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_DELETE_CHILD)
        };

        assertEquals(2, Acls.compact(acl).length);
    }

    @Test
    public void testCompactByPrincipal2() {
        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.GROUP, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_READ_DATA),
        };

        assertEquals(2, Acls.compact(acl).length);
    }

    @Test
    public void testCompactByPrincipal3() {
        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_WRITE_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_WRITE_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_READ_DATA),
        };

        assertEquals(2, Acls.compact(acl).length);
    }

    @Test
    public void testCompactByPrincipal4() {
        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_DELETE_CHILD),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_WRITE_DATA),};

        assertEquals(2, Acls.compact(acl).length);
    }

    @Test
    public void testCompactByPrincipal5() {
        nfsace4[] acl = new nfsace4[]{
            toACE(Acls.OWNER, ACE4_ACCESS_DENIED_ACE_TYPE, ACE4_READ_DATA),
            toACE(Acls.OWNER, ACE4_ACCESS_ALLOWED_ACE_TYPE, ACE4_WRITE_DATA),};

        assertEquals(2, Acls.compact(acl).length);
    }

    private static void assertMode(int expected, int actual) {
        String message = String.format("expected<0%o>, but was <0%o>",
                expected, actual);
        fail(message);
    }
}
