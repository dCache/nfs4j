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

    /**
     * Constructs an {@code ChimeraNFSException} with the specified nfs
     * status code, detail message and cause.
     *
     * @param status nfsstat code
     * @param msg detailed error message
     * @param cause the cause of this exception
     * @since 0.10
     */
    protected ChimeraNFSException(int status, String msg, Throwable cause) {
        super(nfsstat.toString(status) + " : " + msg, cause);
        nfsStatus = status;
    }

    /**
     * Constructs an {@code ChimeraNFSException} with the specified nfs
     * status code and detail message.
     *
     * @param status nfsstat code
     * @param msg detailed error message
     */
    protected ChimeraNFSException(int status, String msg) {
        super(nfsstat.toString(status) +  " : " + msg);
        nfsStatus = status;
    }

    /**
     * Constructs an {@code ChimeraNFSException} with the specified nfs
     * status code and cause.
     *
     * @param status nfsstat code
     * @param cause the cause of this exception
     * @since 0.10
     */
    protected ChimeraNFSException(int status, Throwable cause) {
        super(nfsstat.toString(status), cause);
        nfsStatus = status;
    }

    /**
     * Constructs an {@code ChimeraNFSException} with the specified nfs
     * status code.
     *
     * @param status nfsstat code
     */
    protected ChimeraNFSException(int status) {
        super(nfsstat.toString(status));
        nfsStatus = status;
    }

    /**
     * Returns the nfs status code associated with the exception.
     *
     * @return nfs status code.
     */
    public int getStatus() {
        return nfsStatus;
    }
}
