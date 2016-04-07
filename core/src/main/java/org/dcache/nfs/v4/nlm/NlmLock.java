/*
 * Copyright (c) 2015 Deutsches Elektronen-Synchroton,
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
import com.google.common.collect.Range;
import org.dcache.nfs.v4.xdr.lock_owner4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;

public class NlmLock {

    /**
     * Opaque object that identifies the host or process that is holding the
     * lock.
     */
    private final lock_owner4 owner;

    /**
     * Range of locked region
     */
    private final Range<Long> lockRange;

    /**
     * Type of lock defined by {@link nfs_lock_type4}
     */
    private final int lockType;

    private NlmLock(lock_owner4 owner, int lockType, Range<Long> lockRange) {
        this.owner = owner;
        this.lockType = lockType;
        this.lockRange = lockRange;
    }
    public NlmLock(lock_owner4 owner, int lockType, long offset, long length) {
        this(owner, lockType, length == nfs4_prot.NFS4_UINT64_MAX ? Range.greaterThan(offset) : Range.closedOpen(offset, offset + length));
    }

    public lock_owner4 getOwner() {
        return owner;
    }

    public long getOffset() {
        return lockRange.lowerEndpoint();
    }

    public long getLength() {
        return lockRange.hasUpperBound() ? lockRange.upperEndpoint() - lockRange.lowerEndpoint() : nfs4_prot.NFS4_UINT64_MAX;
    }

    public int getLockType() {
        return lockType;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.owner.hashCode();
        hash = 23 * hash + lockRange.hashCode();
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

        if (!lockRange.equals(other.lockRange)) {
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
        return lockRange.isConnected(other.lockRange);
    }

    public NlmLock mergeLock(NlmLock lock) {

        if(!lockRange.isConnected(lock.lockRange)) {
            throw new RuntimeException("an attempt to merge non owerlapping leck regions");
        }

        if(lockType != lock.lockType) {
            throw new RuntimeException("an attempt to merge different lock types");
        }

        if (!isSameOwner(lock)) {
            throw new RuntimeException("an attempt to merge lock of different owners");
        }

        Range newLockRange = lockRange.span(lock.lockRange);
        return new NlmLock(owner, lockType, newLockRange);
    }

    public NlmLock splitLock(NlmLock lock) {

        if (!lockRange.isConnected(lock.lockRange)) {
            throw new RuntimeException("an attempt to merge non owerlapping leck regions");
        }

        if (lockType != lock.lockType) {
            throw new RuntimeException("an attempt to merge different lock types");
        }

        if (!isSameOwner(lock)) {
            throw new RuntimeException("an attempt to merge lock of different owners");
        }


        return new NlmLock(owner, lockType, lockRange);
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
                .add("lock-range", lockRange)
                .add("owner", owner)
                .add("lock_type", nfs_lock_type4.toString(lockType))
                .toString();
    }

}
