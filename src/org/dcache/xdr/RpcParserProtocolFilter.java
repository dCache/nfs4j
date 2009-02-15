package org.dcache.xdr;

import com.sun.grizzly.ProtocolParser;
import com.sun.grizzly.filter.ParserProtocolFilter;

public class RpcParserProtocolFilter extends  ParserProtocolFilter {

    @Override
    public ProtocolParser newProtocolParser() {
        return new RpcProtocolPaser();
    }
}
