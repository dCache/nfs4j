/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Comparator;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.v4.xdr.SECINFO4resok;
import org.dcache.nfs.v4.xdr.SECINFO_NO_NAME4res;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.qop4;
import org.dcache.nfs.v4.xdr.rpcsec_gss_info;
import org.dcache.nfs.v4.xdr.sec_oid4;
import org.dcache.nfs.v4.xdr.secinfo4;
import org.dcache.nfs.v4.xdr.secinfo_style4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.vfs.Inode;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.rpc.RpcAuthType;
import org.dcache.oncrpc4j.rpc.gss.RpcGssService;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public class OperationSECINFO_NO_NAME extends AbstractNFSv4Operation {

    private final static uint32_t DEFAULT_QOP = new uint32_t(0);
    private final static String K5OID = "1.2.840.113554.1.2.2";

    public OperationSECINFO_NO_NAME(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_SECINFO_NO_NAME);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        try {
            SECINFO_NO_NAME4res res = result.opsecinfo_no_name;
            Inode inode = context.currentInode();
            switch (_args.opsecinfo_no_name.value) {
                case secinfo_style4.SECINFO_STYLE4_PARENT:
		    inode = context.getFs().parentOf(inode);
		    // fall through
                case secinfo_style4.SECINFO_STYLE4_CURRENT_FH:
                    res.status = nfsstat.NFS_OK;
                    res.resok4 = new SECINFO4resok();
                    res.resok4.value = secinfosOf(inode, context);
                    break;
                default:
                    throw new BadXdrException("bad type: " + _args.opsecinfo_no_name.value);
            }
        } catch (GSSException e) {
            throw new NfsIoException(e.getMessage());
        }
	context.clearCurrentInode();
    }

    static secinfo4[] secinfosOf(Inode inode, CompoundContext context) throws GSSException {
        //final sec_oid4 k5Oid = new sec_oid4

        final InetAddress remote = context.getRemoteSocketAddress().getAddress();
        final FsExport.Sec[] exports = context.getExportFile().exportsFor(remote)
                .map(FsExport::getSec)
                .sorted(Comparator.reverseOrder())
                .toArray(FsExport.Sec[]::new);

        final secinfo4[] secinfos = new secinfo4[exports.length];
        for (int i = 0; i < secinfos.length; i++) {
            secinfos[i] = toSecinfo(exports[i]);
        }
        return secinfos;
    }

    private static rpcsec_gss_info gssInfoOf(FsExport.Sec sec) throws GSSException {

        final rpcsec_gss_info gssInfo = new rpcsec_gss_info();
        final Oid oid = new Oid(K5OID);
        gssInfo.oid = new sec_oid4(oid.getDER());
        gssInfo.qop = new qop4(DEFAULT_QOP);
        switch (sec) {
            case KRB5:
                gssInfo.service = RpcGssService.RPC_GSS_SVC_NONE;
                break;
            case KRB5I:
                gssInfo.service = RpcGssService.RPC_GSS_SVC_INTEGRITY;
                break;
            case KRB5P:
                gssInfo.service = RpcGssService.RPC_GSS_SVC_PRIVACY;
                break;
            default:
                throw new IllegalArgumentException("Bad flavor: " + sec);
        }
        return gssInfo;
    }

    private static secinfo4 toSecinfo(FsExport.Sec sec) throws GSSException {
        secinfo4 secinfo = new secinfo4();
        switch(sec) {
            case NONE:
                secinfo.flavor = RpcAuthType.NONE;
                break;
            case SYS:
                secinfo.flavor = RpcAuthType.UNIX;
                break;
            case KRB5:
            case KRB5I:
            case KRB5P:
                secinfo.flavor = RpcAuthType.RPCGSS_SEC;
                secinfo.flavor_info = gssInfoOf(sec);
                break;
            default:
                throw new IllegalArgumentException("Bad flavor: " + sec);
        }

        return secinfo;
    }
}
