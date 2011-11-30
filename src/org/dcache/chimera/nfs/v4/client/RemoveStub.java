/*
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

import org.dcache.chimera.nfs.v4.xdr.REMOVE4args;
import org.dcache.chimera.nfs.v4.xdr.component4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;

public class RemoveStub {


    public static nfs_argop4 generateRequest(String path) {

        REMOVE4args args = new REMOVE4args();

        args.target = new component4();
        args.target.value = new utf8str_cs(path);

        nfs_argop4 op = new nfs_argop4();

        op.argop = nfs_opnum4.OP_REMOVE;
        op.opremove = args;

        return op;

    }

}
