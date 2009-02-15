package org.dcache.xdr;

public class RpcCall implements XdrDecodable {

    private final static int RPCVERS = 2;

    private int _prog;
    private int _version;
    private int _proc;
    private int _rpcvers;
    private RpcAuth _auth;


    public void decode(Xdr xdr) throws XdrException {
        _rpcvers = xdr.get_int();
        _prog = xdr.get_int();
        _version = xdr.get_int();
        if( _rpcvers != RPCVERS ) {
            throw new RpcMismatchException("RPC version mismatch: " + _rpcvers, 2, 2);
        }
        _proc = xdr.get_int();
        int authType = xdr.get_int();
        switch(authType) {
            case RpcAuthType.UNIX :
                _auth = new RpcAuthTypeUnix();
                xdr.decode(_auth);
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(_auth);

        sb.append("RPC ver  : ").append(_rpcvers).append("\n");
        sb.append("Program  : ").append(_prog).append("\n");
        sb.append("Version  : ").append(_version).append("\n");
        sb.append("Procedure: ").append(_proc).append("\n");

        return sb.toString();
    }

    public void encode(Xdr xdr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
