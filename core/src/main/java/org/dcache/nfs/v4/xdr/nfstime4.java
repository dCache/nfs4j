/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.xdr;
import org.dcache.xdr.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;

public class nfstime4 implements XdrAble {
    /**
     * max allowed value for nseconds
     * @see  https://tools.ietf.org/html/rfc5661#section-3.3.1
     */
    private static final int MAX_VALID_NSECONDS = 999999999;

    public long seconds;
    public int nseconds;

    public nfstime4() {
    }

    /**
     * Create a new nfstime4 from given millis.
     * @param millis
     */
    public nfstime4(long millis) {
	seconds = millis / 1000;
	nseconds = (int)((millis % 1000) * 1000000);
    }

    public nfstime4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
	xdr.xdrEncodeLong(seconds);
	xdr.xdrEncodeInt(nseconds);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        seconds = xdr.xdrDecodeLong();
        nseconds = xdr.xdrDecodeInt();
    }

    public long toMillis() throws ChimeraNFSException {
        if (nseconds < 0 || nseconds > MAX_VALID_NSECONDS ) {
            throw  new ChimeraNFSException(nfsstat.NFSERR_INVAL, "Invalid value for nseconds");
        }

        return TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS)
                + TimeUnit.MILLISECONDS.convert(nseconds, TimeUnit.NANOSECONDS);
    }

}
// End of nfstime4.java
