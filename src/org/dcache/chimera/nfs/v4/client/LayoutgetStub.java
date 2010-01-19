package org.dcache.chimera.nfs.v4.client;

import java.io.IOException;

import java.nio.ByteBuffer;
import org.dcache.chimera.nfs.v4.xdr.LAYOUTGET4args;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.length4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.chimera.nfs.v4.xdr.offset4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
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

        XdrDecodingStream xdr = new Xdr(ByteBuffer.wrap(data));
        nfsv4_1_file_layout4 device = new nfsv4_1_file_layout4();

        xdr.beginDecoding();
        device.xdrDecode(xdr);
        xdr.endDecoding();

        return device;
    }

}
