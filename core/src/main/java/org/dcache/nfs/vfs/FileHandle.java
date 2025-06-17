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
package org.dcache.nfs.vfs;

@Deprecated(forRemoval = true)
public class FileHandle extends Inode {
    private final static byte[] EMPTY_FH = new byte[0];

    public FileHandle(byte[] bytes) {
        super(bytes);
    }

    public FileHandle(int generation, int exportIdx, int type, byte[] fs_opaque) {
        super(generation, exportIdx, type, fs_opaque);
    }

    @Deprecated(forRemoval = true)
    public static class FileHandleBuilder {
        private int generation = 0;
        private int export_idx = 0;
        private int type = 0;
        private byte[] fs_opaque = EMPTY_FH;

        public FileHandleBuilder setGeneration(int generation) {
            this.generation = generation;
            return this;
        }

        public FileHandleBuilder setExportIdx(int export_idx) {
            this.export_idx = export_idx;
            return this;
        }

        public FileHandleBuilder setType(int type) {
            this.type = type;
            return this;
        }

        public FileHandleBuilder setFsOpaque(byte[] fs_opaque) {
            this.fs_opaque = fs_opaque;
            return this;
        }

        public FileHandle build(byte[] opaque) {
            return new FileHandle(generation, export_idx, type, opaque);
        }

        public FileHandle build() {
            return build(fs_opaque);
        }
    }

    public int getVersion() {
        return handleVersion();
    }

    public int getMagic() {
        return super.getMagic();
    }

    public int getGeneration() {
        return super.getGeneration();
    }

    public int getExportIdx() {
        return super.getExportIdx();
    }

    public int getType() {
        return super.getType();
    }

    public byte[] getFsOpaque() {
        return super.getFsOpaque();
    }

    public byte[] bytes() {
        return toNfsHandle();
    }
}
