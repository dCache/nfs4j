/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.nfs.v4.xdr;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;
import org.dcache.oncrpc4j.xdr.XdrEncodingStream;
import java.io.IOException;

public class GET_DIR_DELEGATION4args implements XdrAble {
    public boolean gdda_signal_deleg_avail;
    public bitmap4 gdda_notification_types;
    public attr_notice4 gdda_child_attr_delay;
    public attr_notice4 gdda_dir_attr_delay;
    public bitmap4 gdda_child_attributes;
    public bitmap4 gdda_dir_attributes;

    public GET_DIR_DELEGATION4args() {
    }

    public GET_DIR_DELEGATION4args(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeBoolean(gdda_signal_deleg_avail);
        gdda_notification_types.xdrEncode(xdr);
        gdda_child_attr_delay.xdrEncode(xdr);
        gdda_dir_attr_delay.xdrEncode(xdr);
        gdda_child_attributes.xdrEncode(xdr);
        gdda_dir_attributes.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        gdda_signal_deleg_avail = xdr.xdrDecodeBoolean();
        gdda_notification_types = new bitmap4(xdr);
        gdda_child_attr_delay = new attr_notice4(xdr);
        gdda_dir_attr_delay = new attr_notice4(xdr);
        gdda_child_attributes = new bitmap4(xdr);
        gdda_dir_attributes = new bitmap4(xdr);
    }

}
// End of GET_DIR_DELEGATION4args.java
