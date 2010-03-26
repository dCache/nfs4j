/*
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

package org.dcache.xdr;

import java.util.logging.Logger;

public class RpcAuthTypeNone implements RpcAuth, XdrAble {

    private final int _type =  RpcAuthType.NONE;
    private byte[] body;

    private final static Logger _log = Logger.getLogger(RpcAuthTypeNone.class.getName());

    public RpcAuthTypeNone() {
        this(new byte[0]);
    }

    public RpcAuthTypeNone(byte[] body) {
        this.body = body;
    }

    public int type() {
        return _type;
    }

    @Override
    public void xdrDecode(XdrDecodingStream xdr) {
        body = xdr.xdrDecodeDynamicOpaque();
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException {
       xdr.xdrEncodeInt(_type);
       xdr.xdrEncodeDynamicOpaque(body);
    }

}
