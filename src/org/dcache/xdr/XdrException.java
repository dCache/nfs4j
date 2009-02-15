package org.dcache.xdr;

public class XdrException extends Exception implements XdrEncodable {

    private static final long serialVersionUID = 5492424750571897543L;

    private final int _replyStatus;

    XdrException(int replyStatus, String msg) {
        super(msg);
        _replyStatus = replyStatus;
    }


    @Override
    public void encode(Xdr xdr) {
        xdr.put_int(_replyStatus);
    }

}
