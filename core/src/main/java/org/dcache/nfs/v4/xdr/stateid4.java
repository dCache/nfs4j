/*
 * Copyright (c) 2009 - 2020 Deutsches Elektronen-Synchroton,
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
import java.io.Serializable;
import java.util.Arrays;

import org.dcache.nfs.util.Opaque;
import org.dcache.nfs.vfs.OpenHandle;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.util.Bytes;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

import com.google.common.io.BaseEncoding;

public class stateid4 implements XdrAble, Serializable, OpenHandle {

    static final long serialVersionUID = -6677150504723505919L;

    public int seqid;
    public byte[] other;

    public stateid4() {
    }

    public stateid4(byte[] other, int seq) {
        this.other = other;
        seqid = seq;
    }

    public stateid4(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(seqid);
        xdr.xdrEncodeOpaque(other, 12);
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        seqid = xdr.xdrDecodeInt();
        other = xdr.xdrDecodeOpaque(12);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;
        if (!(obj instanceof stateid4))
            return false;

        final stateid4 other_id = (stateid4) obj;

        return Arrays.equals(this.other, other_id.other);
    }

    /**
     * Check state for equality including sequence id.
     *
     * @param otherState
     * @return <tt>true</tt> if and only if oterhState has equal <tt>other</tt> field and sequence id.
     */
    public boolean equalsWithSeq(stateid4 otherState) {

        if (otherState == this) {
            return true;
        }

        return otherState.seqid == this.seqid && Arrays.equals(this.other, otherState.other);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(other);
    }

    public long getClientId() {
        return Bytes.getLong(other, 0);
    }

    @Override
    public Opaque getOpaque() {
        return Opaque.forMutableByteArray(other);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(BaseEncoding.base16().lowerCase().encode(other));
        sb.append(", seq: ").append(seqid).append("]");
        return sb.toString();
    }

}
// End of stateid4.java
