package org.dcache.nfs.v4.xdr;

import org.dcache.nfs.status.InvalException;
import org.junit.Test;

public class offset4Test {

    @Test
    public void shouldSucceedIfSumNotOverflow() throws Exception {
        offset4 offset = new offset4(5);
        length4 length = new length4(Long.MAX_VALUE - 10);
        offset.checkOverflow(length, null);
    }

    @Test(expected = InvalException.class)
    public void shouldfailIfSumOverflow() throws Exception {
        offset4 offset = new offset4(Long.MAX_VALUE - 10);
        length4 length = new length4(11);
        offset.checkOverflow(length, null);
    }

}
