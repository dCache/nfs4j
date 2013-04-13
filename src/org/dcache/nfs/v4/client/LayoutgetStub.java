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
package org.dcache.nfs.v4.client;

import java.io.IOException;

import java.nio.ByteBuffer;
import org.dcache.nfs.v4.xdr.LAYOUTGET4args;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.uint64_t;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrBuffer;
import org.dcache.xdr.XdrDecodingStream;

public class LayoutgetStub {

    public static nfs_argop4 generateRequest(boolean signal_layout_avail,
            int layout_type, int iomode, int offset, int length, int minlength,
            int maxcount, stateid4 stateid) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_LAYOUTGET;
        op.oplayoutget = new LAYOUTGET4args();

        op.oplayoutget.loga_signal_layout_avail = signal_layout_avail;
        op.oplayoutget.loga_layout_type = layout_type;
        op.oplayoutget.loga_iomode = iomode;

        offset4 off = new offset4();
        uint64_t u64 = new uint64_t(offset);
        off.value = u64;

        length4 cnLong = new length4();
        cnLong.value = new uint64_t(length);

        length4 cnZero = new length4();
        cnZero.value = new uint64_t(minlength);

        count4 cnShort = new count4();
        cnShort.value = new uint32_t(maxcount);

        op.oplayoutget.loga_offset = off;
        op.oplayoutget.loga_length = cnLong;
        op.oplayoutget.loga_minlength = cnZero;
        op.oplayoutget.loga_maxcount = cnShort;

        op.oplayoutget.loga_stateid = stateid;

        return op;
    }

    static public nfsv4_1_file_layout4 decodeLayoutId(byte[] data) throws OncRpcException, IOException {

        XdrDecodingStream xdr = new XdrBuffer(data);
        nfsv4_1_file_layout4 device = new nfsv4_1_file_layout4();

        xdr.beginDecoding();
        device.xdrDecode(xdr);
        xdr.endDecoding();

        return device;
    }

}
