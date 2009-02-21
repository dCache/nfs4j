package org.dcache.xdr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import com.sun.grizzly.Context;
import com.sun.grizzly.util.OutputWriter;

public class GrizzlyXdrTransport implements XdrTransport {

    private final Context _context;

    public GrizzlyXdrTransport(Context context) {
        _context = context;
    }


    @Override
    public void send(ByteBuffer data) throws IOException {

        SelectableChannel channel = _context.getSelectionKey().channel();
        OutputWriter.flushChannel(channel, data);

    }

}
