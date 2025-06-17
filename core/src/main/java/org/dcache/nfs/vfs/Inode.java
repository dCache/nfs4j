/*
 * Copyright (c) 2009 - 2024 Deutsches Elektronen-Synchroton,
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
    private final static byte[] FH_V0_REG = new byte[] {0x30, 0x3a};
    private final static byte[] FH_V0_PFS = new byte[] {0x32, 0x35, 0x35, 0x3a};

    private final int version;
    private final int magic;
    private final int generation;
    private final int exportIdx;
    private final int type;
    private final byte[] fs_opaque;

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
        this.fs_opaque = fs_opaque;
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
        if (geussVersion == VERSION) {
            version = geussVersion;
            magic = magic_version & 0x00FFFFFF;
            if (magic != MAGIC) {
                throw new IllegalArgumentException("Bad magic number");
            }

            generation = b.getInt();
            exportIdx = b.getInt();
            type = (int) b.get();
            int olen = (int) b.get();
            fs_opaque = new byte[olen];
            b.get(fs_opaque);

        } else if (arrayEquals(bytes, FH_V0_REG, FH_V0_REG.length)
                || arrayEquals(bytes, FH_V0_PFS, FH_V0_PFS.length)) {
            magic = MAGIC;
            generation = 0;
            type = bytes[1] == FH_V0_REG[1] ? 0 : 1;
            if (type == 1) {
                /*
                 * convert pseudo inode into real one: '255:' => '0:' NOTICE: the converted handle will present himself
                 * as version 1
                 */
                version = 1;
                exportIdx = 0;
                fs_opaque = new byte[bytes.length - 2];
                System.arraycopy(bytes, 2, fs_opaque, 0, fs_opaque.length);
                fs_opaque[0] = 0x30;
            } else {
                version = 0;
                exportIdx = -1;
                fs_opaque = bytes;
            }
        } else {
            throw new IllegalArgumentException("Unsupported version: " + geussVersion);
        }
    }

    public int getVersion() {
        return version;
    }

    public int getMagic() {
        return magic;
    }

    public int getGeneration() {
        return generation;
    }

    public int getExportIdx() {
        return exportIdx;
    }

    public int getType() {
        return type;
    }

    public byte[] getFsOpaque() {
        return fs_opaque;
    }

    public byte[] bytes() {
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
    public String toString() {
        return BaseEncoding.base16().lowerCase().encode(this.bytes());
    }

    private static boolean arrayEquals(byte[] a1, byte[] a2, int len) {
        if (a1.length < len || a2.length < len)
            return false;
        for (int i = 0; i < len; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    public static Inode forNfsHandle(byte[] bytes) {
        return new Inode(bytes);
    }

    public static Inode forFile(byte[] bytes) {
        return new Inode(0, 0, 0, bytes);
    }

    public byte[] getFileId() {
        return getFsOpaque();
    }

    public byte[] toNfsHandle() {
        return bytes();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes());
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
        return Arrays.equals(bytes(), other.bytes());
    }

    public boolean isPseudoInode() {
        return getType() == 1;
    }

    public int exportIndex() {
        return getExportIdx();
    }

    public int handleVersion() {
        return getVersion();
    }
}
