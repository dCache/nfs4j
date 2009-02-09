package org.dcache.door;

import com.sun.grizzly.ProtocolParser;
import com.sun.grizzly.filter.ParserProtocolFilter;

public class AsciiCommandProtocolFilter extends  ParserProtocolFilter {

    @Override
    public ProtocolParser newProtocolParser() {
        return new AsciiCommandProtocolParser();
    }
}
