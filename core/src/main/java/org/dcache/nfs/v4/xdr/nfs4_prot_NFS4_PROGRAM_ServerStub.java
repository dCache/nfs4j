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
package org.dcache.nfs.v4.xdr;

import java.io.IOException;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcDispatchable;
import org.dcache.oncrpc4j.xdr.XdrVoid;

/**
 */
public abstract class nfs4_prot_NFS4_PROGRAM_ServerStub implements RpcDispatchable {

    @Override
    public void dispatchOncRpcCall(RpcCall call)
           throws OncRpcException, IOException {

        int procedure = call.getProcedure();

        switch ( procedure ) {
            case 0: {
                call.retrieveCall(XdrVoid.XDR_VOID);
                NFSPROC4_NULL_4(call);
                call.reply(XdrVoid.XDR_VOID);
                break;
            }
            case 1: {
                COMPOUND4args args$ = new COMPOUND4args();
                call.retrieveCall(args$);
                COMPOUND4res result$ = NFSPROC4_COMPOUND_4(call, args$);
                call.reply(result$);
                break;
            }
            default:
                call.failProcedureUnavailable();
        }
    }

    public abstract void NFSPROC4_NULL_4(RpcCall call$);

    public abstract COMPOUND4res NFSPROC4_COMPOUND_4(RpcCall call$, COMPOUND4args arg1);

}
// End of nfs4_prot_NFS4_PROGRAM_ServerStub.java
