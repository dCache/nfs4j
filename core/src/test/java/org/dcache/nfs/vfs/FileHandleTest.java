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

import com.google.common.io.BaseEncoding;
import java.nio.charset.Charset;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class FileHandleTest {

    private final static Charset ACSII = Charset.forName("ASCII");

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyHandle() {
        new FileHandle(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadVersion() {
        byte[] bytes = BaseEncoding.base16().lowerCase().decode("02caffee00000000ea15b996002e303a494e4f44453a3030303043333732333331373433393234353645423833453434383434453844323844363a30");
        new FileHandle(bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadMagic() {
        byte[] bytes = BaseEncoding.base16().lowerCase().decode("0100000000000000ea15b996002e303a494e4f44453a3030303043333732333331373433393234353645423833453434383434453844323844363a30");
        new FileHandle(bytes);
    }

    @Test
    public void testValidHandleV1() {
        byte[] bytes = BaseEncoding.base16().lowerCase().decode("01caffee00000000ea15b996002e303a494e4f44453a3030303043333732333331373433393234353645423833453434383434453844323844363a30");
        FileHandle fh = new FileHandle(bytes);

        assertEquals(1, fh.getVersion());
        assertEquals(0xCAFFEE, fh.getMagic());
        assertEquals(0, fh.getGeneration());
        byte[] opaque = fh.getFsOpaque();
        assertEquals("/export/data".hashCode(), fh.getExportIdx());
        assertEquals("0:INODE:0000C37233174392456EB83E44844E8D28D6:0", new String(opaque, ACSII));
    }

    @Test
    public void testBuilder() {

        FileHandle fh = new FileHandle.FileHandleBuilder()
                .setExportIdx("/export/data".hashCode())
                .build("0:INODE:0000C37233174392456EB83E44844E8D28D6:0".getBytes(ACSII));

        assertEquals("01caffee00000000ea15b996002e303a494e4f44453a3030303043333732333331373433393234353645423833453434383434453844323844363a30",
                fh.toString());
        assertArrayEquals(BaseEncoding.base16().lowerCase().decode("01caffee00000000ea15b996002e303a494e4f44453a3030303043333732333331373433393234353645423833453434383434453844323844363a30"),
                fh.bytes());
    }

    @Test
    public void testValidHandleV0Regular() {
        String oldId = "0:INODE:0000C37233174392456EB83E44844E8D28D6:0";

        byte[] bytes = oldId.getBytes(ACSII);
        FileHandle fh = new FileHandle(bytes);

        assertEquals(0, fh.getVersion());
        assertEquals(0xCAFFEE, fh.getMagic());
        assertEquals(0, fh.getGeneration());
        byte[] opaque = fh.getFsOpaque();
        assertEquals(-1, fh.getExportIdx());
        assertEquals(0, fh.getType());
        assertEquals(oldId, new String(opaque, ACSII));
    }

    @Test
    public void testValidHandleV0Pseudo() {
        String oldIdPseudo = "255:INODE:0000C37233174392456EB83E44844E8D28D6:0";
        String oldIdReg = "0:INODE:0000C37233174392456EB83E44844E8D28D6:0";

        byte[] bytes = oldIdPseudo.getBytes(ACSII);
        FileHandle fh = new FileHandle(bytes);

        assertEquals(1, fh.getVersion());
        assertEquals(0xCAFFEE, fh.getMagic());
        assertEquals(0, fh.getGeneration());
        byte[] opaque = fh.getFsOpaque();
        assertEquals(0, fh.getExportIdx());
        assertEquals(1, fh.getType());
        assertEquals(oldIdReg, new String(opaque, ACSII));
    }
}