package org.dcache.door;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.ByteBuffer;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.ProtocolParser;
import com.sun.grizzly.util.OutputWriter;

public class DcapProtocolFilter implements ProtocolFilter {

    enum DcapAsciiCommands {
        hello,
        open
    }


    @Override
    public boolean execute(Context context) throws IOException {
        String query = (String) context.removeAttribute(ProtocolParser.MESSAGE);

        if (query == null || query.trim().length() == 0) {
            return false;
        }


        Args args = new Args(query);
        Integer sessionId = Integer.valueOf(0);
        Integer subSessionId = Integer.valueOf(0);

        SelectableChannel channel = context.getSelectionKey().channel();
        try {
            sessionId = Integer.valueOf(args.argv(0));
            subSessionId = Integer.valueOf(args.argv(1));
            DcapAsciiCommands dcapCommand = DcapAsciiCommands.valueOf(args.argv(3));

            System.out.println("processing request = " + args);
            ByteBuffer buffer;
            switch( dcapCommand) {

                case hello:
                    buffer = ByteBuffer.wrap("0 0 server hello 0 0 1 1\n".getBytes());
                    OutputWriter.flushChannel(channel, buffer);
                    break;
                case open:
                    StringBuilder sb =  new StringBuilder();
                    sb.append(sessionId).append(" ").append(subSessionId).append(" server ");
                    sb.append("connect localhost 1111 secret\n");
                    buffer = ByteBuffer.wrap(sb.toString().getBytes());
                    OutputWriter.flushChannel(channel, buffer);
                    break;
            }

        }catch( NumberFormatException e ) {
            ByteBuffer buffer = ByteBuffer.wrap("Protocol violation\n".getBytes());
            OutputWriter.flushChannel(channel, buffer);
            return false;
        }catch(IllegalArgumentException e) {
            System.out.println("could not parse request = " + args);
            StringBuilder sb =  new StringBuilder();
            sb.append(sessionId).append(" ").append(subSessionId).
                append(" server failed 17 \"Protocol violation : unsupported command [").
                append(args.argv(3)).append("]\"\n");
            ByteBuffer buffer = ByteBuffer.wrap( sb.toString().getBytes());
            OutputWriter.flushChannel(channel, buffer);
            return false;
        }


        return true;
    }

    @Override
    public boolean postExecute(Context arg0) throws IOException {
        return true;
    }

}
