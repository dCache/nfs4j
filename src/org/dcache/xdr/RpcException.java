package org.dcache.xdr;


public class RpcException  extends OncRpcException {


    private static final long serialVersionUID = 1050375986800628995L;
    private final RpcRejectedReply _reply;
    
    RpcException(String msg, RpcRejectedReply reply) {
        super(msg);
        _reply = reply;
    }
        
    public RpcRejectedReply getRpcReply() {
        return _reply;
    }
    
}
