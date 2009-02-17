package org.dcache.xdr;

public class RpcCall extends RpcMsg {

    private final static int RPCVERS = 2;

    private int _prog;
    private int _version;
    private int _proc;
    private int _rpcvers;
    private RpcAuth _authVerf;
    private RpcAuth _auth;

    
    public RpcCall(int xid) {
        super(xid, RpcMessageType.CALL);
    }

    public void xdrDecode(Xdr xdr) throws XdrException {
        _rpcvers = xdr.get_int();
        _prog = xdr.get_int();
        _version = xdr.get_int();
        if( _rpcvers != RPCVERS ) {
            throw new RpcException("RPC version mismatch: " + _rpcvers,
                    new RpcMismatchReply(xid(), 2, 2));
        }
        _proc = xdr.get_int();
        int authType = xdr.get_int();
        switch(authType) {
            case RpcAuthType.UNIX :
                _auth = new RpcAuthTypeUnix();
                break;
            case RpcAuthType.NONE:
                _auth = new RpcAuthTypeNone();
        }
        xdr.decode(_auth);

        authType = xdr.get_int();
        switch(authType) {
            case RpcAuthType.UNIX :
                _authVerf = new RpcAuthTypeUnix();
                break;
            case RpcAuthType.NONE:
                _authVerf = new RpcAuthTypeNone();
        }
        xdr.decode(_authVerf);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(_auth);

        sb.append("RPC vers.: ").append(_rpcvers).append("\n");
        sb.append("Program  : ").append(_prog).append("\n");
        sb.append("Version  : ").append(_version).append("\n");
        sb.append("Procedure: ").append(_proc).append("\n");

        return sb.toString();
    }

    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        // TODO Auto-generated method stub        
    }

    
    /**
     * @return the RPC call program id
     */
    public int getProgram() {
        return _prog;
    }

    /**
     * @return the RPC call program version
     */
    public int getProgramVersion() {
        return _version;
    }

    /**
     * @return the RPC call program procedure
     */
    public int getProcedure() {
        return _proc;
    }

    public RpcAuth getAuth() {
        return _auth;
    }
   
    public RpcAuth getAuthVerf() {
        return _authVerf;
    } 
}
