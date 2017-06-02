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
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.v4.xdr.stable_how4;

/**
 * An interface to file system.
 *
 * <p>
 * Object name handling:</p>
 * <p>
 * Many operations takes object name as an argument. For all such operations
 * only a single component, relative to provided parent inode, of a file system
 * hierarchy must be considered. In addition to back end file system
 * implementation restrictions, the following constrains must be applied:
 * <ul>
 * <li>"." and ".." can not be specified as the name when creating a file system
 * object</li>
 * <li>name which contains "/" must be rejected.
 * </ul>
 *
 * @since 0.0.5
 */
public interface VirtualFileSystem {

    /**
     * Check access to file system object.
     *
     * @param inode inode of the object to check.
     * @param mode a mask of permission bits to check.
     * @return an allowed subset of permissions from the given mask.
     * @throws IOException
     */
    int access(Inode inode, int mode) throws IOException;

    /**
     * Create a new object in a given directory with a specific name.
     *
     * @param parent directory where new object must be created.
     * @param type the type of the object to be created.
     * @param name name of the object.
     * @param subject the owner subject of a newly created object.
     * @param mode initial permission mask.
     * @return the inode of the newly created object.
     * @throws IOException
     */
    Inode create(Inode parent, Stat.Type type, String name, Subject subject, int mode) throws IOException;

    /**
     * Get file system's usage information.
     *
     * @return file system's usage information.
     * @throws IOException
     */
    FsStat getFsStat() throws IOException;

    /**
     * Get inode of the root object for given file system.
     *
     * @return inode of the root object.
     * @throws IOException
     */
    Inode getRootInode() throws IOException;

    /**
     * Get inode of the object with a given name in provided directory.
     *
     * @param parent parent directory's inode.
     * @param name object name.
     * @return inode of the object.
     * @throws IOException
     */
    Inode lookup(Inode parent, String name) throws IOException;

    /**
     * Create a hard-link to an existing file system object.
     *
     * @param parent directory, where new object must be created.
     * @param link an inode of existing file system object.
     * @param name name of the new object.
     * @param subject the owner subject of a newly created object.
     * @return inode of the newly created object.
     * @throws IOException
     */
    Inode link(Inode parent, Inode link, String name, Subject subject) throws IOException;

    /**
     * Get list of file system objects in the given directory. The provided
     * {@code cookie} identifies a logical offset of the listing, if directory
     * listing is processed in chunks. The {@code verifier} argument used to
     * validate cookies as directory content can be changes and earlier generated
     * cookie cannot be used any more. For initial listing a zero cookie and verifier
     * is used. The returned listing will contain only entries with cookies
     * greater than specified value.
     *
     * @param inode inode of the directory to list.
     * @param verifier opaque verifier to identify {@code snapshot} to list.
     * @param cookie a logical offset in the listing.
     * @return DirectoryStream containing directory listing.
     * @throws IOException
     */
    DirectoryStream list(Inode inode, byte[] verifier, long cookie) throws IOException;

    /**
     * Generate a opaque directory verifier which is identified with can
     * be used as identifier of directory's state snapshot.
     * @param inode inode of the directory to create verifier.
     * @return opaque verifier.
     * @throws IOException
     */
    byte[] directoryVerifier(Inode inode) throws IOException;

    /**
     * Create a new sub-directory in a given directory.
     *
     * @param parent directory, where new sub-directory must be created.
     * @param name the name of the newly created sub-directory.
     * @param subject the owner subject of a newly created sub-directory.
     * @param mode initial permission mask.
     * @return inode of the newly created sub-directory.
     * @throws IOException
     */
    Inode mkdir(Inode parent, String name, Subject subject, int mode) throws IOException;

    /**
     * Move file system object from one directory to another.
     *
     * @param src the directory from which to move the object.
     * @param oldName object's name in the source directory.
     * @param dest the directory where the file system object should be moved.
     * @param newName object's name in the new directory.
     * @return true if file system was changed.
     * @throws IOException
     */
    boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException;

    /**
     * Get parent directory of a given object.
     *
     * @param inode inode of a file system object for which the parent inode is
     * desired.
     * @return parent directory of the given object.
     * @throws IOException
     */
    Inode parentOf(Inode inode) throws IOException;

    /**
     * Read data from file with a given inode into {@code data}.
     *
     * @param inode inode of the file to read from.
     * @param data byte array for writing.
     * @param offset file's position to read from.
     * @param count number of bytes to read.
     * @return number of bytes read from the file, possibly zero. -1 if EOF is
     * reached.
     * @throws IOException
     */
    int read(Inode inode, byte[] data, long offset, int count) throws IOException;

    /**
     * Get value of a symbolic link object.
     *
     * @param inode symbolic link's inode.
     * @return value of a symbolic link.
     * @throws IOException
     */
    String readlink(Inode inode) throws IOException;

    /**
     * Remove a file system object from the given directory and possibly the
     * object itself.
     *
     * @param parent directory from which file system object is removed.
     * @param name object's name in the directory.
     * @throws IOException
     */
    void remove(Inode parent, String name) throws IOException;

    /**
     * Create a symbolic link.
     *
     * @param parent inode of the directory, where symbolic link is created.
     * @param name name of the symbolic link object.
     * @param link the value the symbolic link points to.
     * @param subject the owner subject of a newly created object.
     * @param mode initial permission mask.
     * @return inode of newly created object.
     * @throws IOException
     */
    Inode symlink(Inode parent, String name, String link, Subject subject, int mode) throws IOException;

    /**
     * Write provided {@code data} into inode with a given stability level.
     *
     * @param inode inode of the file to write.
     * @param data data to be written.
     * @param offset the file position to begin writing at.
     * @param count number of bytes to write.
     * @param stabilityLevel data stability level.
     * @return write result.
     * @throws IOException
     */
    WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel) throws IOException;

    /**
     * Flush data in {@code dirty} state to the stable storage. Typically
     * follows {@link #write()} operation.
     *
     * @param inode inode of the file to commit.
     * @param offset the file position to start commit at.
     * @param count number of bytes to commit.
     * @throws IOException
     */
    void commit(Inode inode, long offset, int count) throws IOException;

    /**
     * Get file system object's attributes.
     *
     * @param inode inode of the file system object.
     * @return file's attributes.
     * @throws IOException
     */
    Stat getattr(Inode inode) throws IOException;

    /**
     * Set/update file system object's attributes.
     *
     * @param inode inode of the file system object.
     * @param stat file's attributes to set.
     * @throws IOException
     */
    void setattr(Inode inode, Stat stat) throws IOException;

    /**
     * Get file system object's Access Control List.
     *
     * @param inode inode of the file system object.
     * @return object's access control list.
     * @throws IOException
     */
    nfsace4[] getAcl(Inode inode) throws IOException;

    /**
     * Set file system object's Access Control List.
     *
     * @param inode inode of the file system object.
     * @param acl access control list to set.
     * @throws IOException
     */
    void setAcl(Inode inode, nfsace4[] acl) throws IOException;

    /**
     * Returns true if file system object eligible for pNFS operations.
     *
     * @param inode inode of the file system object to test for PNFS operations.
     * @return true if file system object eligible for pNFS operations.
     * @throws IOException
     */
    boolean hasIOLayout(Inode inode) throws IOException;

    /**
     * Get instance of a {@link AclCheckable} object which can perform access
     * control list check.
     *
     * @return instance of AclCheckable.
     */
    AclCheckable getAclCheckable();

    /**
     * Get instance of a {@link NfsIdMapping} object which can provide principal
     * identity mapping.
     *
     * @return instance of NfsIdMapping.
     */
    NfsIdMapping getIdMapper();

    /**
     * Object which represents write operation result.
     */
    public static class WriteResult {

        private final int bytesWritten;
        private final StabilityLevel stabilityLevel;

        public WriteResult(StabilityLevel stabilityLevel, int bytesWritten) {
            this.stabilityLevel = stabilityLevel;
            this.bytesWritten = bytesWritten;
        }

        /**
         * Get number of written bytes.
         *
         * @return number of written bytes.
         */
        public int getBytesWritten() {
            return bytesWritten;
        }

        /**
         * Get stability level of written data.
         *
         * @return write stability level.
         */
        public StabilityLevel getStabilityLevel() {
            return stabilityLevel;
        }
    }

    //NOTE - stability values and ordinals are the same for nfs 3 and 4
    /**
     * Write stability level.
     */
    public static enum StabilityLevel {

        /**
         * Data not on a stable storage and can be lost on data server crash.
         */
        UNSTABLE,
        /**
         * Data is synchronized with stable storage.
         */
        DATA_SYNC,
        /**
         * Data and file's attributes are synchronized with stable storage.
         */
        FILE_SYNC;

        /**
         * Get {@link StabilityLevel} corresponding to provided {@code how}
         * field of nfs write call.
         *
         * @param stableHowValue the value of nfs {@code stable_how} to get
         * StabilityLevel.
         * @return stability Level.
         * @throws IllegalArgumentException if enum type has no constant with
         * corresponding value.
         */
        public static StabilityLevel fromStableHow(int stableHowValue) {
            switch (stableHowValue) {
                case stable_how4.UNSTABLE4:
                    return UNSTABLE;
                case stable_how4.DATA_SYNC4:
                    return DATA_SYNC;
                case stable_how4.FILE_SYNC4:
                    return FILE_SYNC;
                default:
                    throw new IllegalArgumentException("unhandled stability value " + stableHowValue);
            }
        }

        /**
         * Get nfs {@code how} value corresponding to this StabilityLevel.
         *
         * @return value corresponding to this StabilityLevel.
         */
        public int toStableHow() {
            return ordinal();
        }
    }
}
