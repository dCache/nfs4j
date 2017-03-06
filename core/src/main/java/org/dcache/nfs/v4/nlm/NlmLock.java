/*
 * Copyright (c) 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.nlm;

import com.google.common.base.MoreObjects;
import org.dcache.nfs.v4.StateOwner;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;

public class NlmLock {

    /**
     * Opaque object that identifies the host or process that is holding the
     * lock.
     */
    private final StateOwner owner;
    /**
     * Identifies offset where locked region starts
     */
    private final long offset;
    /**
     * The length of locked region.
     */
    private final long length;

    /**
     * Type of lock defined by {@link nfs_lock_type4}
     */
    private final int lockType;

    public NlmLock(StateOwner owner, int lockType, long offset, long length) {
        this.owner = owner;
        this.offset = offset;
        this.length = length;
        this.lockType = lockType;
    }

    public StateOwner getOwner() {
        return owner;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    public int getLockType() {
        return lockType;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.owner.hashCode();
        hash = 23 * hash + (int) (this.offset ^ (this.offset >>> 32));
        hash = 23 * hash + (int) (this.length ^ (this.length >>> 32));
        hash = 23 * hash + (int) (this.lockType ^ (this.lockType >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NlmLock other = (NlmLock) obj;
        if (this.offset != other.offset) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        if (this.lockType != other.lockType) {
            return false;
        }
        return this.owner.equals(other.owner);
    }

    public boolean isSameOwner(NlmLock other) {
        return this.owner.equals(other.owner);
    }

    public boolean isOverlappingRange(NlmLock other) {
        // both lock up-to the end
        if (other.length == nfs4_prot.NFS4_UINT64_MAX && length == nfs4_prot.NFS4_UINT64_MAX) {
            return true;
        }

        /*
         * use subtraction to avoid positive long overflow, e.g.
         * instead of
         *     A + B < C
         *  use
         *     C - A > B
         */

        if (other.length == nfs4_prot.NFS4_UINT64_MAX) {
            /*
             * either this lock region starts at higher offset,
             * or lock region doesn't ends before other one starts.
             */
            return offset > other.offset || other.offset - length < offset;
        }

        if (length == nfs4_prot.NFS4_UINT64_MAX ){
            /**
             * either other lock region starts at higher offset,
             * or lock region doesn't ends before this one starts.
             */
            return other.offset > offset || offset - other.length < other.offset;
        }

        if (offset > other.offset) {
            return offset - other.offset < other.length;
        }

        if (other.offset > offset) {
            return other.offset - offset < length;
        }

        // both locks have the same offset
        return true;
    }

    public boolean isConflictingType(NlmLock other) {
        return lockType == nfs_lock_type4 .WRITE_LT || lockType == nfs_lock_type4 .WRITEW_LT
                || other.lockType == nfs_lock_type4 .WRITE_LT || other.lockType == nfs_lock_type4 .WRITEW_LT;
    }

    public boolean isConflicting(NlmLock other) {
        return isOverlappingRange(other) && !isSameOwner(other) && isConflictingType(other);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Lock")
                .add("offset", offset)
                .add("length", length)
                .add("owner", owner)
                .add("lock_type", nfs_lock_type4.toString(lockType))
                .toString();
    }

}
