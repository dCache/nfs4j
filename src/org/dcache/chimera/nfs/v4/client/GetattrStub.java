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
package org.dcache.chimera.nfs.v4.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcache.chimera.nfs.v4.xdr.GETATTR4args;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.fattr4_fs_locations;
import org.dcache.chimera.nfs.v4.xdr.fattr4_mode;
import org.dcache.chimera.nfs.v4.xdr.fattr4_type;
import org.dcache.chimera.nfs.v4.xdr.mode4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrBuffer;
import org.dcache.xdr.XdrDecodingStream;

public class GetattrStub {


    public static nfs_argop4 generateRequest(int ... attrs) {


        nfs_argop4 op = new nfs_argop4();
        GETATTR4args args = new GETATTR4args();

        args.attr_request = bitmap4.of(attrs);
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

        XdrDecodingStream xdr = new XdrBuffer(attributes.attr_vals.value);
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
                String new_owner = owner.toString();
                attr.put(fattr,new_owner );
                break;
            case nfs4_prot.FATTR4_OWNER_GROUP :
                // TODO: use princilat
                utf8str_cs owner_group = new utf8str_cs ();
                owner_group.xdrDecode(xdr);
                String new_group = owner_group.toString();
                attr.put(fattr,new_group );
                break;
            case nfs4_prot.FATTR4_TYPE :
                fattr4_type type = new fattr4_type();
                type.xdrDecode(xdr);
                attr.put(fattr,type );
                break;
            case nfs4_prot.FATTR4_FS_LOCATIONS:
                fattr4_fs_locations fs_locations = new fattr4_fs_locations();
                fs_locations.xdrDecode(xdr);
                attr.put(fattr, fs_locations);
                break;
        }


    }

}
