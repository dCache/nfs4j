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
    public void xdrEncode(Xdr xdr) throws XdrException {
        // TODO Auto-generated method stub
        super.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStats.MSG_DENIED); 
    }

}
