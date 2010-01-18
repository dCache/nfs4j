package org.dcache.chimera.nfs;


public class ExportClient {


    public enum Root {
        TRUSTED, NOTTRUSTED
    }

    public enum IO {
        RW, RO
    }

    private final String _ip;
    private final Root _isTrusted;
    private final IO _rw;

    public ExportClient(String ip, Root isTrusted, IO rw) {

        _ip = ip;
        _isTrusted = isTrusted;
        _rw = rw;

    }

    public String ip() {
        return _ip;
    }

    public IO io() {
        return _rw;
    }

    public Root trusted() {
        return _isTrusted;
    }

}
