package org.dcache.nfs.vfs;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

public class StatTest {
    @Test
    public void testToString() {
        Stat stat = new Stat();
        stat.setNlink(7);
        stat.setUid(1);
        stat.setGid(2);
        stat.setSize(3L);
        // 01-feb-2003 14:15
        Calendar localCal = Calendar.getInstance(); //machine-specific time zone
        localCal.set(Calendar.YEAR, 2003);
        localCal.set(Calendar.DAY_OF_MONTH, 1);
        localCal.set(Calendar.MONTH, Calendar.FEBRUARY);
        localCal.set(Calendar.HOUR_OF_DAY, 14);
        localCal.set(Calendar.MINUTE, 15);
        localCal.set(Calendar.SECOND, 0);
        localCal.set(Calendar.MILLISECOND, 0);
        stat.setMTime(localCal.getTimeInMillis());
        stat.setMode(0755 | Stat.S_IFDIR);
        Assert.assertEquals("drwxr-xr-x    7    1    2    3 Feb 01 14:15", stat.toString());
        stat.setMode(0401 | Stat.S_IFREG);
        stat.setNlink(6666);
        stat.setSize(1024*16);
        localCal.set(Calendar.DAY_OF_MONTH, 29);
        localCal.set(Calendar.HOUR_OF_DAY, 1);
        stat.setMTime(localCal.getTimeInMillis());
        Assert.assertEquals("-r-------x 6666    1    2  16K Mar 01 01:15", stat.toString());
        stat.setMode(0070 | Stat.S_IFLNK);
        stat.setSize(1024*1024*1024*1024L - 1); //one byte short of 1TB
        Assert.assertEquals("l---rwx--- 6666    1    2 1024G Mar 01 01:15", stat.toString());
    }

    @Test
    public void testSizeToString() {
        Assert.assertEquals("0",Stat.sizeToString(0));
        Assert.assertEquals("1023",Stat.sizeToString(1024-1));
        Assert.assertEquals("1K",Stat.sizeToString(1024));
        Assert.assertEquals("1K",Stat.sizeToString(1024+1));
        Assert.assertEquals("1K",Stat.sizeToString(1024+51));
        Assert.assertEquals("1.1K",Stat.sizeToString(1024+52)); //just after 1.05, round up
        Assert.assertEquals("1024M",Stat.sizeToString(1024*1024*1024-1));
        Assert.assertEquals("1G",Stat.sizeToString(1024*1024*1024));
        Assert.assertEquals("8E",Stat.sizeToString(Long.MAX_VALUE));
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
        ObjectOutputStream os = new ObjectOutputStream(byteArrayOutputStream);
        os.writeObject(stat);
        os.close();

        byte[] serialized = byteArrayOutputStream.toByteArray();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(serialized));

        Stat deserialized = (Stat) is.readObject();

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(stat.getNlink(), deserialized.getNlink());
        Assert.assertEquals(stat.getUid(), deserialized.getUid());
        Assert.assertEquals(stat.getGid(), deserialized.getGid());
        Assert.assertEquals(stat.getSize(), deserialized.getSize());
        Assert.assertEquals(stat.getMTime(), deserialized.getMTime());
        Assert.assertEquals(stat.getMode(), deserialized.getMode());
    }

    @Test
    public void testClone() {
        Stat stat = new Stat();
        stat.setUid(1);
        stat.setMode(0755 | Stat.S_IFDIR);
        Stat clone = stat.clone();
        Assert.assertFalse(stat==clone);
        Assert.assertEquals(stat.getUid(), clone.getUid());
        clone.setUid(42);
        Assert.assertEquals(1, stat.getUid());
        Assert.assertEquals(Stat.Type.DIRECTORY, clone.type());
        clone.setMode(0111 | Stat.S_IFREG);
        Assert.assertEquals(Stat.Type.DIRECTORY, stat.type());
        Assert.assertEquals(Stat.Type.REGULAR, clone.type());
    }
}
