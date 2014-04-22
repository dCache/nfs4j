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
package org.dcache.nfs;

/**
 * A base class for all NFS exceptions. We do not allow direct instances to
 * enforce correct exceptions.
 */
public abstract class ChimeraNFSException extends java.io.IOException {

    /**
     *
     */
    private static final long serialVersionUID = 4319461664218810541L;
    private final int nfsStatus;

    protected ChimeraNFSException(int status ,String msg) {
        super(nfsstat.toString(status) +  " : " + msg);
        nfsStatus = status;
    }

    protected ChimeraNFSException(int status) {
        super(nfsstat.toString(status));
        nfsStatus = status;
    }

    public int getStatus() {
        return nfsStatus;
    }
}
