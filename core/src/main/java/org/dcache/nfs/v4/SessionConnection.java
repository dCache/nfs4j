package org.dcache.nfs.v4;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * An abstraction to represent NFSv4.1 session connection
 */
public class SessionConnection {

    private final InetSocketAddress _local;
    private final InetSocketAddress _remote;

    public SessionConnection(InetSocketAddress local, InetSocketAddress remote) {
        _local = local;
        _remote = remote;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_local, _remote);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SessionConnection other = (SessionConnection) obj;
        return Objects.equals(_local, other._local) && Objects.equals(_remote, other._remote);
    }

    public InetSocketAddress getLocalConnection() {
        return _local;
    }

    public InetSocketAddress getRemoteConnection() {
        return _remote;
    }

}
