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
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;

public class CREATE_SESSION4args implements XdrAble {
    public clientid4 csa_clientid;
    public sequenceid4 csa_sequence;
    public uint32_t csa_flags;
    public channel_attrs4 csa_fore_chan_attrs;
    public channel_attrs4 csa_back_chan_attrs;
    public uint32_t csa_cb_program;
    public callback_sec_parms4 [] csa_sec_parms;

    public CREATE_SESSION4args() {
    }

    public CREATE_SESSION4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        csa_clientid.xdrEncode(xdr);
        csa_sequence.xdrEncode(xdr);
        csa_flags.xdrEncode(xdr);
        csa_fore_chan_attrs.xdrEncode(xdr);
        csa_back_chan_attrs.xdrEncode(xdr);
        csa_cb_program.xdrEncode(xdr);
        { int $size = csa_sec_parms.length; xdr.xdrEncodeInt($size); for ( int $idx = 0; $idx < $size; ++$idx ) { csa_sec_parms[$idx].xdrEncode(xdr); } }
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        csa_clientid = new clientid4(xdr);
        csa_sequence = new sequenceid4(xdr);
        csa_flags = new uint32_t(xdr);
        csa_fore_chan_attrs = new channel_attrs4(xdr);
        csa_back_chan_attrs = new channel_attrs4(xdr);
        csa_cb_program = new uint32_t(xdr);
        { int $size = xdr.xdrDecodeInt(); csa_sec_parms = new callback_sec_parms4[$size]; for ( int $idx = 0; $idx < $size; ++$idx ) { csa_sec_parms[$idx] = new callback_sec_parms4(xdr); } }
    }

}
// End of CREATE_SESSION4args.java
