/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.vfs;

import java.io.IOException;
import javax.security.auth.Subject;
import org.dcache.nfs.ChimeraNFSException;

/**
 * In interface to a component which can validate ACL for a filesystem.
 */
public interface AclCheckable {

    enum Access {
        ALLOW,
        DENY,
        UNDEFINED
    }

    /**
     * An implementation of {@link AclCheckable}, which all always grand the access
     * to an inode.
     */
    public static final AclCheckable ALLOW_ALL = (Subject subject, Inode inode, int accessMask) -> Access.ALLOW;

    /**
     * An implementation of {@link AclCheckable}, which all always deny the access
     * to an inode.
     */
    public static final AclCheckable DENY_ALL = (Subject subject, Inode inode, int accessMask) -> Access.DENY;

    /**
     * An implementation of {@link AclCheckable}, which all always return "no matching ACE".
     */
    public static final AclCheckable UNDEFINED_ALL = (Subject subject, Inode inode, int accessMask) -> Access.UNDEFINED;

    /**
     * Validate requested {@code accessMask} mask for a given {@code subject} and {code inode}.
     *
     * @param subject who performs the access
     * @param inode access to which is performed
     * @param accessMask mask to evaluate
     * @return <tt>ALLOW</tt>, if matching allow ACE is found, <tt>DENY</tt>, if matching deny ACE is found or
     * <tt>UNDEFIED</tt> if no matching ACE found.
     * @throws IOException
     */
    Access checkAcl(Subject subject, Inode inode, int accessMask) throws ChimeraNFSException, IOException;
}
