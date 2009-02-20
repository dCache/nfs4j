package org.dcache.xdr;

public abstract class OncRpcException extends Exception {

    private static final long serialVersionUID = 5492424750571897543L;

    OncRpcException(String msg) {
        super(msg);
    }
}
