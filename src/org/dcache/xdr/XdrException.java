package org.dcache.xdr;

public class XdrException extends Exception implements XdrAble {

    private static final long serialVersionUID = 5492424750571897543L;

    private final int _replyStatus;

    XdrException(int replyStatus, String msg) {
        super(msg);
        _replyStatus = replyStatus;
    }

    @Override
    public void xdrDecode(Xdr xdr) throws XdrException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void xdrEncode(Xdr xdr) throws XdrException {
        xdr.put_int(_replyStatus);        
    }

}
