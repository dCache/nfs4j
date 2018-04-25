/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;
import java.io.Serializable;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class state_owner4 implements XdrAble, Serializable {

    private static final long serialVersionUID = -7146149387990285155L;
    public clientid4 clientid;
    public byte [] owner;

    public state_owner4() {
    }

    public state_owner4(state_owner4  stateOwner) {
        clientid = stateOwner.clientid;
        owner = stateOwner.owner;
    }

    public state_owner4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        clientid.xdrEncode(xdr);
        xdr.xdrEncodeDynamicOpaque(owner);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        clientid = new clientid4(xdr);
        owner = xdr.xdrDecodeDynamicOpaque();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.clientid);
        hash = 67 * hash + Arrays.hashCode(this.owner);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final state_owner4 other = (state_owner4) obj;

        return this.clientid.value == other.clientid.value && Arrays.equals(this.owner, other.owner);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass().getSimpleName())
                .add("clientid", Long.toString(clientid.value, 16))
                .add("ownerid", BaseEncoding.base16().lowerCase().encode(owner))
                .toString();
    }
}
// End of state_owner4.java
