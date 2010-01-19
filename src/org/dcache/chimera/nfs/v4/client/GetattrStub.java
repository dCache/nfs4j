package org.dcache.chimera.nfs.v4.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcache.chimera.nfs.v4.xdr.GETATTR4args;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.fattr4_type;
import org.dcache.chimera.nfs.v4.xdr.mode4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrDecodingStream;

public class GetattrStub {


    public static nfs_argop4 generateRequest(List<Integer> attrs) {


        nfs_argop4 op = new nfs_argop4();
        GETATTR4args args = new GETATTR4args();

        args.attr_request = new bitmap4();
        args.attr_request.value = new uint32_t[2];
        args.attr_request.value[0] = new uint32_t();
        args.attr_request.value[1] = new uint32_t();

        for( Integer mask : attrs) {
            int bit = mask -(32*(mask/32));
            args.attr_request.value[mask/32].value |= 1 << bit;
        }

        op.argop = nfs_opnum4.OP_GETATTR;
        op.opgetattr = args;

        return op;

    }

    public static List<Integer> supportedAttrs(bitmap4 bitmap) {

        List<Integer> supported = new ArrayList<Integer>();

        // TODO:

        return supported;

    }


    public static Map<Integer, Object> decodeType(fattr4 attributes) throws OncRpcException, IOException  {

        Map<Integer,Object> attr = new HashMap<Integer, Object>();


        int[] mask = new int[attributes.attrmask.value.length];
        for( int i = 0; i < mask.length; i++) {
            mask[i] = attributes.attrmask.value[i].value;
        }

        XdrDecodingStream xdr = new Xdr(ByteBuffer.wrap(attributes.attr_vals.value));
        xdr.beginDecoding();

        if( mask.length != 0 ) {
            int maxAttr = 32*mask.length;
            for( int i = 0; i < maxAttr; i++) {
                int newmask = (mask[i/32] >> (i-(32*(i/32))) );
                if( (newmask & 1L) != 0 ) {
                    xdr2fattr(attr, i, xdr);
                }
            }
        }

        xdr.endDecoding();

        return attr;
    }


    static void xdr2fattr( Map<Integer,Object> attr, int fattr , XdrDecodingStream xdr) throws OncRpcException, IOException {

        System.out.println("decoding mask " + fattr);


        switch(fattr) {

            case nfs4_prot.FATTR4_SIZE :
                uint64_t size = new uint64_t();
                size.xdrDecode(xdr);
                attr.put(fattr, size);
                break;
            case nfs4_prot.FATTR4_MODE :
                mode4 mode = new mode4();
                mode.xdrDecode(xdr);
                attr.put(fattr, mode);
                break;
            case nfs4_prot.FATTR4_OWNER :
                // TODO: use princilat
                utf8str_cs owner = new utf8str_cs ();
                owner.xdrDecode(xdr);
                String new_owner = new String(owner.value.value);
                attr.put(fattr,new_owner );
                break;
            case nfs4_prot.FATTR4_OWNER_GROUP :
                // TODO: use princilat
                utf8str_cs owner_group = new utf8str_cs ();
                owner_group.xdrDecode(xdr);
                String new_group = new String(owner_group.value.value);
                attr.put(fattr,new_group );
                break;
            case nfs4_prot.FATTR4_TYPE :
                fattr4_type type = new fattr4_type();
                type.xdrDecode(xdr);
                attr.put(fattr,type );

        }


    }

}
