package org.dcache.xdr;


public class RpcException  extends XdrException {


    private static final long serialVersionUID = 1050375986800628995L;
    private final RpcReply _reply;
    
    RpcException(String msg, RpcReply reply) {
        super(msg);
        _reply = reply;
    }
        
    public RpcReply getRpcReply() {
        return _reply;
    }
    
}
