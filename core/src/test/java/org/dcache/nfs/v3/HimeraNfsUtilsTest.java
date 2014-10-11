package org.dcache.nfs.v3;

import org.dcache.nfs.v3.xdr.nfstime3;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class HimeraNfsUtilsTest {
    @Test
    public void testConvertTimestamp() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        long timestamp = dateFormat.parse("01/02/2003 04:06:06.789").getTime();
        nfstime3 converted = HimeraNfsUtils.convertTimestamp(timestamp);
        Assert.assertEquals(timestamp/1000, converted.seconds.value);
        Assert.assertEquals(1000000 * (timestamp%1000), converted.nseconds.value);
        long decoded = HimeraNfsUtils.convertTimestamp(converted);
        Assert.assertEquals(timestamp, decoded);
    }
}
