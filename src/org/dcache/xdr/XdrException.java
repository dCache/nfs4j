package org.dcache.xdr;

public abstract class XdrException extends Exception {

    private static final long serialVersionUID = 5492424750571897543L;

    XdrException(String msg) {
        super(msg);
    }
}
