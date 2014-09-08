package org.dcache.nfs.vfs;

import java.util.Calendar;

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
}
