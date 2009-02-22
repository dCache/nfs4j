package org.dcache.xdr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RpcCall implements XdrAble {

    private final static Logger _log = Logger.getLogger(RpcCall.class.getName());

    /**
     * Supported RPC protocol version
     */
    private final static int RPCVERS = 2;

    /**
     * RPC program number
     */
    private int _prog;

    /**
     * RPC program version number
     */
    private int _version;

    /**
     * RPC program procedure number
     */
    private int _proc;

    /**
     *  RPC protocol version number
     */
    private int _rpcvers;

    private RpcAuth _authVerf;
    private RpcAuth _auth;

    /**
     * transport to reply messages back
     */
    private final XdrTransport _transport;

    private int _xid;

    public RpcCall(int xid, XdrTransport transport) {
        _transport = transport;
        _xid = xid;
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        _rpcvers = xdr.xdrDecodeInt();
        _prog = xdr.xdrDecodeInt();
        _version = xdr.xdrDecodeInt();
        if( _rpcvers != RPCVERS ) {
            throw new RpcException("RPC version mismatch: " + _rpcvers,
                    new RpcMismatchReply(2, 2));
        }
        _proc = xdr.xdrDecodeInt();
        int authType = xdr.xdrDecodeInt();
        _log.log(Level.FINE, "Auth type: " + authType);
        switch(authType) {
            case RpcAuthType.UNIX :
                _auth = new RpcAuthTypeUnix();
                break;
            case RpcAuthType.NONE:
                _auth = new RpcAuthTypeNone();
                break;
            default:
                throw new RpcException("Unsupported Auth type: " + authType,
                        new RpcAuthMissmatch(RpcAuthStat.AUTH_FAILED));
        }
        _auth.xdrDecode(xdr);

        authType = xdr.xdrDecodeInt();
        _log.log(Level.FINE, "Auth type: " + authType);
        switch(authType) {
            case RpcAuthType.UNIX :
                _authVerf = new RpcAuthTypeUnix();
                break;
            case RpcAuthType.NONE:
                _authVerf = new RpcAuthTypeNone();
                break;
            default:
                throw new RpcException("Unsupported Auth type: " + authType,
                        new RpcAuthMissmatch(RpcAuthStat.AUTH_FAILED));
        }

        _authVerf.xdrDecode(xdr);
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
    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException {
        // TODO Auto-generated method stub
    }


    /**
     * Get RPC call program number.
     *
     * @return version number
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

    /**
     * Get RPC transport used by this call.
     *
     * @return
     */
    public XdrTransport getTransport() {
        return _transport;
    }

    /**
     * Retrieves the parameters sent within an ONC/RPC call message.
     *
     * @param xdr Xdr data which contains call argument.
     * @param args the call argument do decode
     * @throws OncRpcException
     */
    public void retrieveCallArgs(XdrDecodingStream xdr, XdrAble args)
        throws OncRpcException, IOException {
       args.xdrDecode(xdr);
       xdr.endDecoding();
    }


    public void failProgramMismatch(int min, int max) {
        _reply(RpcAccepsStatus.PROG_MISMATCH, new MismatchInfo(min, max));
    }

    public void failProgramUnavailable() {
        _reply(RpcAccepsStatus.PROG_UNAVAIL,  XdrVoid.XDR_VOID);
    }

    public void failProcedureUnavailable() {
        _reply(RpcAccepsStatus.PROC_UNAVAIL,  XdrVoid.XDR_VOID);
    }
    /**
     * Send accepted reply to the client.
     *
     * @param reply
     */
    public void reply(XdrAble reply) {
        _reply(RpcAccepsStatus.SUCCESS, reply);
    }
    private void _reply(int state, XdrAble reply) {
        XdrEncodingStream xdr = new Xdr(8192);

        try {
            xdr.beginEncoding();
            xdr.xdrEncodeInt(_xid);
            xdr.xdrEncodeInt(RpcMessageType.REPLY);
            xdr.xdrEncodeInt(RpcReplyStats.MSG_ACCEPTED);
            getAuthVerf().xdrEncode(xdr);
            xdr.xdrEncodeInt(state);
            reply.xdrEncode(xdr);
            xdr.endEncoding();

            ByteBuffer message = xdr.body();
            _transport.send(message);

        } catch (OncRpcException e) {
            _log.log(Level.WARNING, "Xdr exception: ", e);
        } catch (IOException e) {
            _log.log(Level.SEVERE, "Failed send reply: ", e);
        }
    }

    /**
     * Send rejected reply to the client.
     *
     * @param reply
     */
    public void reject(RpcRejectedReply reply) {
        XdrEncodingStream xdr = new Xdr(1024);

        try {
            xdr.beginEncoding();
            xdr.xdrEncodeInt(_xid);
            xdr.xdrEncodeInt(RpcMessageType.REPLY);
            xdr.xdrEncodeInt(RpcReplyStats.MSG_DENIED);
            reply.xdrEncode(xdr);
            xdr.endEncoding();

            ByteBuffer message = xdr.body();
            _transport.send(message);

        } catch (IOException e) {
            _log.log(Level.SEVERE, "Failed send reply: ", e);
        }
    }

    public void retrieveCall(XdrAble args, XdrDecodingStream xdr) throws OncRpcException, IOException {
        args.xdrDecode(xdr);
        xdr.endDecoding();
    }

}
