package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.Context;
import com.sun.grizzly.util.OutputWriter;

public class GrizzlyXdrTransport implements XdrTransport {

    private final Context _context;
    private final InetSocketAddress _remote;
    private final InetSocketAddress _local;

    private final static Logger _log = Logger.getLogger(GrizzlyXdrTransport.class.getName());

    public GrizzlyXdrTransport(Context context) {
        _context = context;
        SocketChannel socketChannel = ((SocketChannel)context.getSelectionKey().channel());
        _local =(InetSocketAddress) socketChannel.socket().getLocalSocketAddress();
        _remote =(InetSocketAddress)socketChannel.socket().getRemoteSocketAddress();
        _log.log(Level.FINE, "RPC call: remote/local: {0}/{1}", new Object[] { _remote,  _local } );
    }


    @Override
    public void send(ByteBuffer data) throws IOException {

        _log.log(Level.FINE, "reply sent: {0}", data);
        SelectableChannel channel = _context.getSelectionKey().channel();
        OutputWriter.flushChannel(channel, data);
    }


    public InetSocketAddress getLocalSocketAddress() {
        return _local;
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return _remote;
    }
}
