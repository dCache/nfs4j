package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 *
 * Abstraction for sending reply to clients
 *
 */
public interface XdrTransport {

    /**
     * Send data to remote end point.
     *
     * @param data
     * @throws IOException
     */
    public void send(ByteBuffer data) throws IOException;

    /**
     * Get local end point.
     *
     * @return InetSocketAddress of local socket end point
     */
    public InetSocketAddress getLocalSocketAddress();

    /**
     * Get remote end point.
     *
     * @return InetSocketAddress of remote socket end point.
     */
    public InetSocketAddress getRemoteSocketAddress();

}
