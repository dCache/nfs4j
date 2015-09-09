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

import org.dcache.nfs.v4.xdr.READDIR4args;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.count4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_cookie4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.verifier4;

public class ReaddirStub {


    public static nfs_argop4 generateRequest( long cookie, verifier4 verifier, int dircount, int maxcount) {


        nfs_argop4 op = new nfs_argop4();
        op.opreaddir = new READDIR4args();
        op.opreaddir.cookie = new nfs_cookie4(cookie) ;
        op.opreaddir.dircount = new count4(dircount);
        op.opreaddir.maxcount = new count4(maxcount);
        op.opreaddir.attr_request = new bitmap4( new int[] {0, 0});
        op.opreaddir.cookieverf =  verifier;

        op.argop = nfs_opnum4.OP_READDIR;

        return op;

    }


}
