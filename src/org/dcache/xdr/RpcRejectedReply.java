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


/**
 *  Reply to an RPC call that was rejected by the server:
 *
 *  The call can be rejected for two reasons: either the server is not
 *  running a compatible version of the RPC protocol (RPC_MISMATCH), or
 *  the server rejects the identity of the caller (AUTH_ERROR). In case
 *  of an RPC version mismatch, the server returns the lowest and highest
 *  supported RPC version numbers.  In case of invalid authentication,
 *  failure status is returned.
 *
 */
public abstract class RpcRejectedReply extends RpcReply {


    /* (non-Javadoc)
     * @see org.dcache.xdr.RpcReply#xdrEncode(org.dcache.xdr.Xdr)
     */
    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        // TODO Auto-generated method stub
        super.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStats.MSG_DENIED);
    }

}
