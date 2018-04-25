/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v3.xdr;
import java.io.IOException;

import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.rpc.RpcDispatchable;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.xdr.XdrVoid;

/**
 */
public abstract class nfs3_protServerStub implements RpcDispatchable {

    @Override
    public void dispatchOncRpcCall(RpcCall call)
            throws OncRpcException, IOException {

        int procedure = call.getProcedure();

        switch ( procedure ) {
            case 0: {
                call.retrieveCall(XdrVoid.XDR_VOID);
                NFSPROC3_NULL_3(call);
                call.reply(XdrVoid.XDR_VOID);
                break;
            }
            case 1: {
                GETATTR3args args$ = new GETATTR3args();
                call.retrieveCall(args$);
                GETATTR3res result$ = NFSPROC3_GETATTR_3(call, args$);
                call.reply(result$);
                break;
            }
            case 2: {
                SETATTR3args args$ = new SETATTR3args();
                call.retrieveCall(args$);
                SETATTR3res result$ = NFSPROC3_SETATTR_3(call, args$);
                call.reply(result$);
                break;
            }
            case 3: {
                LOOKUP3args args$ = new LOOKUP3args();
                call.retrieveCall(args$);
                LOOKUP3res result$ = NFSPROC3_LOOKUP_3(call, args$);
                call.reply(result$);
                break;
            }
            case 4: {
                ACCESS3args args$ = new ACCESS3args();
                call.retrieveCall(args$);
                ACCESS3res result$ = NFSPROC3_ACCESS_3(call, args$);
                call.reply(result$);
                break;
            }
            case 5: {
                READLINK3args args$ = new READLINK3args();
                call.retrieveCall(args$);
                READLINK3res result$ = NFSPROC3_READLINK_3(call, args$);
                call.reply(result$);
                break;
            }
            case 6: {
                READ3args args$ = new READ3args();
                call.retrieveCall(args$);
                READ3res result$ = NFSPROC3_READ_3(call, args$);
                call.reply(result$);
                break;
            }
            case 7: {
                WRITE3args args$ = new WRITE3args();
                call.retrieveCall(args$);
                WRITE3res result$ = NFSPROC3_WRITE_3(call, args$);
                call.reply(result$);
                break;
            }
            case 8: {
                CREATE3args args$ = new CREATE3args();
                call.retrieveCall(args$);
                CREATE3res result$ = NFSPROC3_CREATE_3(call, args$);
                call.reply(result$);
                break;
            }
            case 9: {
                MKDIR3args args$ = new MKDIR3args();
                call.retrieveCall(args$);
                MKDIR3res result$ = NFSPROC3_MKDIR_3(call, args$);
                call.reply(result$);
                break;
            }
            case 10: {
                SYMLINK3args args$ = new SYMLINK3args();
                call.retrieveCall(args$);
                SYMLINK3res result$ = NFSPROC3_SYMLINK_3(call, args$);
                call.reply(result$);
                break;
            }
            case 11: {
                MKNOD3args args$ = new MKNOD3args();
                call.retrieveCall(args$);
                MKNOD3res result$ = NFSPROC3_MKNOD_3(call, args$);
                call.reply(result$);
                break;
            }
            case 12: {
                REMOVE3args args$ = new REMOVE3args();
                call.retrieveCall(args$);
                REMOVE3res result$ = NFSPROC3_REMOVE_3(call, args$);
                call.reply(result$);
                break;
            }
            case 13: {
                RMDIR3args args$ = new RMDIR3args();
                call.retrieveCall(args$);
                RMDIR3res result$ = NFSPROC3_RMDIR_3(call, args$);
                call.reply(result$);
                break;
            }
            case 14: {
                RENAME3args args$ = new RENAME3args();
                call.retrieveCall(args$);
                RENAME3res result$ = NFSPROC3_RENAME_3(call, args$);
                call.reply(result$);
                break;
            }
            case 15: {
                LINK3args args$ = new LINK3args();
                call.retrieveCall(args$);
                LINK3res result$ = NFSPROC3_LINK_3(call, args$);
                call.reply(result$);
                break;
            }
            case 16: {
                READDIR3args args$ = new READDIR3args();
                call.retrieveCall(args$);
                READDIR3res result$ = NFSPROC3_READDIR_3(call, args$);
                call.reply(result$);
                break;
            }
            case 17: {
                READDIRPLUS3args args$ = new READDIRPLUS3args();
                call.retrieveCall(args$);
                READDIRPLUS3res result$ = NFSPROC3_READDIRPLUS_3(call, args$);
                call.reply(result$);
                break;
            }
            case 18: {
                FSSTAT3args args$ = new FSSTAT3args();
                call.retrieveCall(args$);
                FSSTAT3res result$ = NFSPROC3_FSSTAT_3(call, args$);
                call.reply(result$);
                break;
            }
            case 19: {
                FSINFO3args args$ = new FSINFO3args();
                call.retrieveCall(args$);
                FSINFO3res result$ = NFSPROC3_FSINFO_3(call, args$);
                call.reply(result$);
                break;
            }
            case 20: {
                PATHCONF3args args$ = new PATHCONF3args();
                call.retrieveCall(args$);
                PATHCONF3res result$ = NFSPROC3_PATHCONF_3(call, args$);
                call.reply(result$);
                break;
            }
            case 21: {
                COMMIT3args args$ = new COMMIT3args();
                call.retrieveCall(args$);
                COMMIT3res result$ = NFSPROC3_COMMIT_3(call, args$);
                call.reply(result$);
                break;
            }
            default:
                call.failProcedureUnavailable();
        }
    }

    public abstract void NFSPROC3_NULL_3(RpcCall call$);

    public abstract GETATTR3res NFSPROC3_GETATTR_3(RpcCall call$, GETATTR3args arg1);

    public abstract SETATTR3res NFSPROC3_SETATTR_3(RpcCall call$, SETATTR3args arg1);

    public abstract LOOKUP3res NFSPROC3_LOOKUP_3(RpcCall call$, LOOKUP3args arg1);

    public abstract ACCESS3res NFSPROC3_ACCESS_3(RpcCall call$, ACCESS3args arg1);

    public abstract READLINK3res NFSPROC3_READLINK_3(RpcCall call$, READLINK3args arg1);

    public abstract READ3res NFSPROC3_READ_3(RpcCall call$, READ3args arg1);

    public abstract WRITE3res NFSPROC3_WRITE_3(RpcCall call$, WRITE3args arg1);

    public abstract CREATE3res NFSPROC3_CREATE_3(RpcCall call$, CREATE3args arg1);

    public abstract MKDIR3res NFSPROC3_MKDIR_3(RpcCall call$, MKDIR3args arg1);

    public abstract SYMLINK3res NFSPROC3_SYMLINK_3(RpcCall call$, SYMLINK3args arg1);

    public abstract MKNOD3res NFSPROC3_MKNOD_3(RpcCall call$, MKNOD3args arg1);

    public abstract REMOVE3res NFSPROC3_REMOVE_3(RpcCall call$, REMOVE3args arg1);

    public abstract RMDIR3res NFSPROC3_RMDIR_3(RpcCall call$, RMDIR3args arg1);

    public abstract RENAME3res NFSPROC3_RENAME_3(RpcCall call$, RENAME3args arg1);

    public abstract LINK3res NFSPROC3_LINK_3(RpcCall call$, LINK3args arg1);

    public abstract READDIR3res NFSPROC3_READDIR_3(RpcCall call$, READDIR3args arg1);

    public abstract READDIRPLUS3res NFSPROC3_READDIRPLUS_3(RpcCall call$, READDIRPLUS3args arg1);

    public abstract FSSTAT3res NFSPROC3_FSSTAT_3(RpcCall call$, FSSTAT3args arg1);

    public abstract FSINFO3res NFSPROC3_FSINFO_3(RpcCall call$, FSINFO3args arg1);

    public abstract PATHCONF3res NFSPROC3_PATHCONF_3(RpcCall call$, PATHCONF3args arg1);

    public abstract COMMIT3res NFSPROC3_COMMIT_3(RpcCall call$, COMMIT3args arg1);

}
// End of nfs3_protServerStub.java
