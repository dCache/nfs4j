/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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
import java.util.ArrayList;
import java.util.List;

import org.dcache.nfs.v4.xdr.CREATE4args;
import org.dcache.nfs.v4.xdr.attrlist4;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.component4;
import org.dcache.nfs.v4.xdr.createtype4;
import org.dcache.nfs.v4.xdr.fattr4;
import org.dcache.nfs.v4.xdr.fattr4_mode;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_ftype4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.xdr.XdrBuffer;
import org.glassfish.grizzly.Buffer;

public class MkdirStub {

    public static nfs_argop4 generateRequest(String path) {


        CREATE4args args = new CREATE4args();

        args.objname = new component4(path);
        args.objtype = new createtype4();
        args.objtype.type = nfs_ftype4.NF4DIR;
        args.createattrs = new fattr4();

        args.createattrs.attr_vals = new attrlist4();
        args.createattrs.attr_vals.value = openAttrs();

        args.createattrs.attrmask = openFattrBitmap();


        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_CREATE;
        op.opcreate = args;

        return op;

    }


    private static bitmap4 openFattrBitmap() {

        List<Integer> attrs = new ArrayList<>();

        attrs.add(nfs4_prot.FATTR4_MODE);

        bitmap4 afttrBitmap = new bitmap4(new int[2]);

        for( Integer mask : attrs) {
            int bit;
            int bitmap;
            if( mask > 31 ) {
                bit = mask - 32;
                bitmap = afttrBitmap.value[1];
            }else{
                bit = mask;
                bitmap = afttrBitmap.value[0];
            }

            bitmap |= 1 << bit;

        }

        return afttrBitmap;
    }

    private static byte[] openAttrs() {

        XdrBuffer xdr = new XdrBuffer(1024);
        try {
            xdr.beginEncoding();
            fattr4_mode mode = new fattr4_mode(0755);
            mode.xdrEncode(xdr);

        }catch(IOException never_happens) {
            // ignore
        }

        xdr.endEncoding();
        Buffer b = xdr.asBuffer();
        byte[] retBytes = new byte[b.remaining()] ;
        b.get(retBytes);

        return retBytes;
    }

}
