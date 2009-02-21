package org.dcache.xdr;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * Abstraction for sending reply to clients
 *
 */
public interface XdrTransport {

    public void send(ByteBuffer data) throws IOException;

}
