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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.security.Principal;
import javax.security.auth.Subject;
import org.dcache.auth.Subjects;

/**
 *
 * @author tigran
 */
public class NssIdmapper implements NfsIdMapping, NfsLoginService {

    private final String _domain;
    private final int NOBODY_UID = -1;
    private final int NOBODY_GID = -1;
    private final String NOBODY_USER = "nobody";
    private final String NOBODY_GROUP = "nobody";

    public NssIdmapper(String domain) {
        _domain = domain;
    }

    @Override
    public String gidToPrincipal(int id) {
        __group g = _libc.getgrgid(id);
        if( g == null)
            return NOBODY_GROUP;
        return addDomain(g.name);
    }

    @Override
    public int principalToGid(String principal) {
        __group g = _libc.getgrnam(stripDomain(principal));
        if(g == null)
            return NOBODY_GID;
        return g.gid;
    }

    @Override
    public int principalToUid(String principal) {
        __password p = _libc.getpwnam(stripDomain(principal));
        if(p == null)
            return NOBODY_UID;
        return  p.uid;
    }

    @Override
    public String uidToPrincipal(int id) {
         __password p = _libc.getpwuid(id);
         if(p == null)
             return NOBODY_USER;
         return addDomain(p.name);
    }

    @Override
    public Subject login(Principal principal) {
        __password p = _libc.getpwnam(stripDomain(principal.getName()));
        if(p == null)
            return Subjects.NOBODY;
        return Subjects.of(p.uid, p.gid, groupsOf(p));
    }

    /**
     * handle to libc.
     */
    private final LibC _libc = (LibC) Native.loadLibrary("c", LibC.class);

    private int[] groupsOf(__password pwrecord) {

        boolean done = false;
        int[] groups = new int[0];
        while (!done) {
            IntByReference ngroups = new IntByReference();
            ngroups.setValue(groups.length);
            if (_libc.getgrouplist(pwrecord.name, pwrecord.gid, groups, ngroups) < 0) {
                groups = new int[ngroups.getValue()];
                continue;
            }
            done = true;
        }

        return groups;
    }

    /*
     * struct passwd equivalent  as defined in <pwd.h>
     */
    static public class __password extends Structure {

        public String name;
        public String passwd;
        public int uid;
        public int gid;
        public String gecos;
        public String dir;
        public String shell;
    }

    /*
     * struct group equivalent as defined in <pwd.h>
     */
    static public class __group extends Structure {

        public String name;
        public String passwd;
        public int gid;
        public Pointer mem;
    }

    /*
     * hook required functions from libc
     */
    public interface LibC extends Library {

        __password getpwnam(String name);

        __password getpwuid(int id);

        __group getgrnam(String name);

        __group getgrgid(int id);

        int getgrouplist(String user, int gid, int[] groups, IntByReference ngroups);
    }

    private String stripDomain(String s) {
        int n = s.indexOf('@');
        if (n != -1) {
            return s.substring(0, n);
        }
        return s;
    }

    private String addDomain(String s) {
        return s + "@" + _domain;
    }
}