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

import org.dcache.nfs.util.Opaque;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;

public class stateid4 implements XdrAble, Serializable, Cloneable {

    static final long serialVersionUID = -6677150504723505919L;

    private int seqid;
    private final Opaque other;

    public static stateid4 forBytes(byte[] bytes, int seqid) {
        return new stateid4(seqid, Opaque.forBytes(bytes));
    }

    public stateid4(XdrDecodingStream xdr) throws OncRpcException, IOException {
        this.seqid = xdr.xdrDecodeInt();
        this.other = Opaque.forBytes(xdr.xdrDecodeOpaque(12));
    }

    @Deprecated(forRemoval = true)
    public stateid4(byte[] bytes, int seqid) {
        this(seqid, Opaque.forBytes(bytes));
    }

    private stateid4(int seqid, Opaque other) {
        this.seqid = seqid;
        this.other = other.toImmutableOpaque();
    }

    public Opaque getOpaque() {
        return other;
    }

    public int getSeqId() {
        return seqid;
    }

    public long getClientId() {
        return other.longAt(0);
    }

    public static long getClientId(Opaque stateIdOther) {
        return stateIdOther.longAt(0);
    }

    public int getType() {
        return other.byteAt(11);
    }

    @Override
    public stateid4 clone() {
        return new stateid4(seqid, other);
    }

    public void xdrEncode(XdrEncodingStream xdr)
            throws OncRpcException, IOException {
        xdr.xdrEncodeInt(seqid);
        xdr.xdrEncodeOpaque(other.toBytes(), 12);
    }

    public void xdrDecode(XdrDecodingStream xdr)
            throws OncRpcException, IOException {
        throw new UnsupportedOperationException("Use constructor");
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;
        if (!(obj instanceof stateid4))
            return false;

        final stateid4 other_id = (stateid4) obj;

        return this.other.equals(other_id.other);
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

        return otherState.seqid == this.seqid && this.other.equals(otherState.other);
    }

    @Override
    public int hashCode() {
        return this.other.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(other);
        sb.append(", seq: ").append(seqid).append("]");
        return sb.toString();
    }

    public void bumpSeqid() {
        ++seqid;
    }
}
// End of stateid4.java
