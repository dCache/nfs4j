/*
 * Copyright (c) 2009 - 2025 Deutsches Elektronen-Synchroton,
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.dcache.nfs.util.Opaque;

import com.google.common.io.BaseEncoding;

/**
 * NFS file handle on wire representation format v1.
 *
 * <pre>
 *   byte fh_version;      // file handle format version number; version 1 description
 *   byte[3] fh_magic      // 0xcaffee
 *   uint32 fh_generation; // server boot time or 0 for permanent handles
 *   uint32 export_index;    // index into export table
 *   byte fh_type          // 1 if pseudo fs
 *   byte fh_olen;         // length of opaque data
 *   byte[] fh_opaque;     // FS specific opaque data <= 114
 * </pre>
 */
public class Inode {
    private final static int MIN_LEN = 14;
    private final static int VERSION = 1;
    private final static int MAGIC = 0xCAFFEE;

    private final int version;
    private final int magic;
    private final int generation;
    private final int exportIdx;
    private final int type;
    private final byte[] nfsHandle;

    private final Opaque opaqueKey;

    @Deprecated(forRemoval = true)
    public Inode(FileHandle fh) {
        this(fh.bytes());
    }

    /**
     * This constructor will become marked {@code protected} in a future version.
     *
     * @param generation The handle generation (e.g., server boot time), or {@code 0} for permanent handles
     * @param exportIdx The index into the export table
     * @param type 1=pseudo FS
     * @param fs_opaque FS specific opaque data (maximum 114 bytes)
     */
    @Deprecated
    public Inode(int generation, int exportIdx, int type, byte[] fs_opaque) {
        this.version = VERSION;
        this.magic = MAGIC;
        this.generation = generation;
        this.exportIdx = exportIdx;
        this.type = type;
        this.opaqueKey = Opaque.forBytes(fs_opaque);

        this.nfsHandle = buildNfsHandle(fs_opaque);
    }

    /**
     * This constructor will become marked {@code protected} in a future version.
     *
     * @param bytes The VFS-specific bytes.
     */
    @Deprecated
    public Inode(byte[] bytes) {
        if (bytes.length < MIN_LEN) {
            throw new IllegalArgumentException("too short");
        }

        ByteBuffer b = ByteBuffer.wrap(bytes);
        b.order(ByteOrder.BIG_ENDIAN);

        int magic_version = b.getInt();
        int geussVersion = (magic_version & 0xFF000000) >>> 24;
        if (geussVersion != VERSION) {
            throw new IllegalArgumentException("Unsupported version: " + geussVersion);
        }

        version = geussVersion;
        magic = magic_version & 0x00FFFFFF;
        if (magic != MAGIC) {
            throw new IllegalArgumentException("Bad magic number");
        }

        generation = b.getInt();
        exportIdx = b.getInt();
        type = (int) b.get();
        int olen = (int) b.get();
        this.opaqueKey = Opaque.forBytes(b, 0, olen);

        this.nfsHandle = bytes.clone();
    }

    @Deprecated(forRemoval = true)
    protected int getMagic() {
        return magic;
    }

    @Deprecated(forRemoval = true)
    protected int getGeneration() {
        return generation;
    }

    @Deprecated(forRemoval = true)
    protected int getType() {
        return type;
    }

    @Override
    public String toString() {
        return BaseEncoding.base16().lowerCase().encode(nfsHandle);
    }

    public static Inode forNfsHandle(byte[] bytes) {
        return new Inode(bytes);
    }

    public static Inode forFile(byte[] bytes) {
        return new Inode(0, 0, 0, bytes);
    }

    public static Inode forFileIdKey(Opaque key) {
        return forFile(key.toBytes());
    }

    @Deprecated(forRemoval = true)
    public byte[] getFileId() {
        return opaqueKey.toBytes();
    }

    /**
     * Returns a locking-key referring to the underlying inode/file referred to by this instance, or a superset,
     * suitable for {@link org.dcache.nfs.v4.nlm.LockManager} etc.
     * <p>
     * This may or may not be equal to {@link #getFileIdKey()}.
     * 
     * @return The locking key for file referred to by this inode.
     */
    public Opaque getLockKey() {
        return opaqueKey;
    }

    /**
     * Returns a key suitable for identifying the underlying inode/file referred to by this instance, providing a
     * {@link Object#equals(Object)} and {@link Object#hashCode()} implementation that may or may not be different from
     * {@link Inode#equals(Object)} and {@link Inode#hashCode()}.
     * 
     * @return The fileId key.
     */
    public Opaque getFileIdKey() {
        return opaqueKey;
    }

    public byte[] toNfsHandle() {
        return nfsHandle.clone();
    }

    private byte[] buildNfsHandle(byte[] fs_opaque) {
        int len = fs_opaque.length + MIN_LEN;
        byte[] bytes = new byte[len];
        ByteBuffer b = ByteBuffer.wrap(bytes);
        b.order(ByteOrder.BIG_ENDIAN);

        b.putInt(version << 24 | magic);
        b.putInt(generation);
        b.putInt(exportIdx);
        b.put((byte) type);
        b.put((byte) fs_opaque.length);
        b.put(fs_opaque);
        return bytes;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nfsHandle);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Inode)) {
            return false;
        }
        final Inode other = (Inode) obj;
        return Arrays.equals(nfsHandle, other.nfsHandle);
    }

    public boolean isPseudoInode() {
        return type == 1;
    }

    public int exportIndex() {
        return exportIdx;
    }

    public int handleVersion() {
        return version;
    }
}
