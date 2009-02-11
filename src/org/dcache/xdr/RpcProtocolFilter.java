package org.dcache.xdr;

import com.sun.grizzly.ProtocolParser;
import com.sun.grizzly.filter.ParserProtocolFilter;

public class RpcProtocolFilter extends  ParserProtocolFilter {

    @Override
    public ProtocolParser newProtocolParser() {
        return new RpcProtocolPaser();
    }
}
