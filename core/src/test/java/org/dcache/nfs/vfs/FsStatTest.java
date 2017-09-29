package org.dcache.nfs.vfs;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FsStatTest {
    @Test
    public void testSerialization() throws Exception {
        FsStat fsStat = new FsStat(1, 2, 3, 4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	try (ObjectOutputStream os = new ObjectOutputStream(byteArrayOutputStream)) {
	    os.writeObject(fsStat);
	}

        byte[] serialized = byteArrayOutputStream.toByteArray();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(serialized));

        FsStat deserialized = (FsStat) is.readObject();

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(fsStat.getTotalSpace(), deserialized.getTotalSpace());
        Assert.assertEquals(fsStat.getTotalFiles(), deserialized.getTotalFiles());
        Assert.assertEquals(fsStat.getUsedSpace(), deserialized.getUsedSpace());
        Assert.assertEquals(fsStat.getUsedFiles(), deserialized.getUsedFiles());
    }
}
