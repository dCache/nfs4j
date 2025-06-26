package org.dcache.nfs.vfs;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.dcache.nfs.vfs.Stat.StatAttribute;
import org.junit.Assert;
import org.junit.Test;

public class StatTest {
    @Test
    public void testToString() {
        Stat stat = new Stat();
        stat.setNlink(7);
        stat.setUid(1);
        stat.setGid(2);
        stat.setSize(3L);
        // 01-feb-2003 14:15
        Calendar localCal = Calendar.getInstance(); // machine-specific time zone
        localCal.set(Calendar.YEAR, 2003);
        localCal.set(Calendar.DAY_OF_MONTH, 1);
        localCal.set(Calendar.MONTH, Calendar.FEBRUARY);
        localCal.set(Calendar.HOUR_OF_DAY, 14);
        localCal.set(Calendar.MINUTE, 15);
        localCal.set(Calendar.SECOND, 0);
        localCal.set(Calendar.MILLISECOND, 0);
        stat.setMTime(localCal.getTimeInMillis());
        stat.setMode(0755 | Stat.S_IFDIR);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm");
        assertEquals("drwxr-xr-x    7    1    2    3 " + formatter.format(localCal.getTime()), stat.toString());
        stat.setMode(0401 | Stat.S_IFREG);
        stat.setNlink(6666);
        stat.setSize(1024 * 16);
        localCal.set(Calendar.DAY_OF_MONTH, 29);
        localCal.set(Calendar.HOUR_OF_DAY, 1);
        stat.setMTime(localCal.getTimeInMillis());
        assertEquals("-r-------x 6666    1    2  16K " + formatter.format(localCal.getTime()), stat.toString());
        stat.setMode(0070 | Stat.S_IFLNK);
        stat.setSize(1024 * 1024 * 1024 * 1024L - 1); // one byte short of 1TB
        assertEquals("l---rwx--- 6666    1    2 1024G " + formatter.format(localCal.getTime()), stat.toString());
    }

    @Test
    public void testSizeToString() {
        assertEquals("0", Stat.sizeToString(0));
        assertEquals("1023", Stat.sizeToString(1024 - 1));
        assertEquals("1K", Stat.sizeToString(1024));
        assertEquals("1K", Stat.sizeToString(1024 + 1));
        assertEquals("1K", Stat.sizeToString(1024 + 51));
        // localized format to handle ',' vs '.' (German vs US English)
        assertEquals(String.format("%.1fK", 1.1), Stat.sizeToString(1024 + 52)); // just after 1.05, round up
        assertEquals("1024M", Stat.sizeToString(1024 * 1024 * 1024 - 1));
        assertEquals("1G", Stat.sizeToString(1024 * 1024 * 1024));
        assertEquals("8E", Stat.sizeToString(Long.MAX_VALUE));
    }

    @Test
    public void testSerialization() throws Exception {
        Stat stat = new Stat();
        stat.setNlink(7);
        stat.setUid(1);
        stat.setGid(2);
        stat.setSize(3L);
        stat.setMTime(System.currentTimeMillis());
        stat.setMode(0755 | Stat.S_IFDIR);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(byteArrayOutputStream)) {
            os.writeObject(stat);
        }

        byte[] serialized = byteArrayOutputStream.toByteArray();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(serialized));

        Stat deserialized = (Stat) is.readObject();

        assertNotNull(deserialized);
        assertEquals(stat.getNlink(), deserialized.getNlink());
        assertEquals(stat.getUid(), deserialized.getUid());
        assertEquals(stat.getGid(), deserialized.getGid());
        assertEquals(stat.getSize(), deserialized.getSize());
        assertEquals(stat.getMTime(), deserialized.getMTime());
        assertEquals(stat.getMode(), deserialized.getMode());
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetDev() {
        new Stat().getDev();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetIno() {
        new Stat().getIno();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetMode() {
        new Stat().getMode();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetNlink() {
        new Stat().getNlink();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetUid() {
        new Stat().getUid();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetGid() {
        new Stat().getGid();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetRdev() {
        new Stat().getRdev();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetSize() {
        new Stat().getSize();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetATime() {
        new Stat().getATime();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetMTime() {
        new Stat().getMTime();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetCTime() {
        new Stat().getCTime();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetGeneration() {
        new Stat().getGeneration();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedType() {
        new Stat().type();
    }

    @Test
    public void testGetDev() {
        Stat stat = new Stat();
        stat.setDev(1);
        assertEquals(1, stat.getDev());
    }

    @Test(expected = IllegalStateException.class)
    public void testNotDefinedGetBtime() {
        new Stat().getBTime();
    }

    @Test
    public void testGetIno() {
        Stat stat = new Stat();
        assertFalse(stat.isDefined(StatAttribute.INO));
        stat.setIno(1);
        assertTrue(stat.isDefined(StatAttribute.INO));
        assertEquals(1, stat.getIno());

    }

    @Test
    public void testGetMode() {
        Stat stat = new Stat();
        stat.setMode(0755 | Stat.S_IFDIR);
        assertEquals(0755 | Stat.S_IFDIR, stat.getMode());
        assertEquals(Stat.Type.DIRECTORY, stat.type());
    }

    @Test
    public void testGetNlink() {
        Stat stat = new Stat();
        stat.setNlink(1);
        assertEquals(1, stat.getNlink());

    }

    @Test
    public void testGetUid() {
        Stat stat = new Stat();
        stat.setUid(1);
        assertEquals(1, stat.getUid());
    }

    @Test
    public void testGetGid() {
        Stat stat = new Stat();
        stat.setGid(1);
        assertEquals(1, stat.getGid());
    }

    @Test
    public void testGetRdev() {
        Stat stat = new Stat();
        stat.setRdev(1);
        assertEquals(1, stat.getRdev());
    }

    @Test
    public void testGetSize() {
        Stat stat = new Stat();
        stat.setSize(1);
        assertEquals(1, stat.getSize());
    }

    @Test
    public void testGetATime() {
        Stat stat = new Stat();
        stat.setATime(1);
        assertEquals(1, stat.getATime());
    }

    @Test
    public void testGetMTime() {
        Stat stat = new Stat();
        stat.setMTime(1);
        assertEquals(1, stat.getMTime());
    }

    @Test
    public void testGetCTime() {
        Stat stat = new Stat();
        stat.setCTime(1);
        assertEquals(1, stat.getCTime());
    }

    @Test
    public void testGetGeneration() {
        Stat stat = new Stat();
        stat.setGeneration(1);
        assertEquals(1, stat.getGeneration());
    }

    @Test
    public void testGetBtime() {
        Stat stat = new Stat();
        stat.setBTime(1);
        assertEquals(1, stat.getBTime());
    }

    @Test
    public void testClone() {
        Stat stat = new Stat();
        stat.setUid(1);
        stat.setMode(0755 | Stat.S_IFDIR);
        Stat clone = stat.clone();
        Assert.assertFalse(stat == clone);
        Assert.assertEquals(stat.getUid(), clone.getUid());
        clone.setUid(42);
        Assert.assertEquals(1, stat.getUid());
        Assert.assertEquals(Stat.Type.DIRECTORY, clone.type());
        clone.setMode(0111 | Stat.S_IFREG);
        Assert.assertEquals(Stat.Type.DIRECTORY, stat.type());
        Assert.assertEquals(Stat.Type.REGULAR, clone.type());
    }
}
