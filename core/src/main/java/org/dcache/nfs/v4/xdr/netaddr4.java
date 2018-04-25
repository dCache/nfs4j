/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.rpc.net.InetSocketAddresses;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class netaddr4 implements XdrAble {
    public String na_r_netid;
    public String na_r_addr;

    public netaddr4() {
    }

    public netaddr4(InetSocketAddress socketAddress) {
        na_r_addr = InetSocketAddresses.uaddrOf(socketAddress);
        na_r_netid = InetSocketAddresses.tcpNetidOf(socketAddress.getAddress());
    }

    public netaddr4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeString(na_r_netid);
        xdr.xdrEncodeString(na_r_addr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        na_r_netid = xdr.xdrDecodeString();
        na_r_addr = xdr.xdrDecodeString();
    }

    @Override
    public String toString() {
        return na_r_netid + "://" + na_r_addr;
    }
}
// End of netaddr4.java
