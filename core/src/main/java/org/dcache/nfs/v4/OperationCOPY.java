/*
 * Copyright (c) 2021 Deutsches Elektronen-Synchroton,
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Uninterruptibles;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.OpenModeException;
import org.dcache.nfs.v4.xdr.COPY4res;
import org.dcache.nfs.v4.xdr.COPY4resok;
import org.dcache.nfs.v4.xdr.copy_requirements4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.v4.xdr.stable_how4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.write_response4;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
/**
 * NFSv4.2 operations that handles server side copy as specified in rfc7862#section-4.
 */
public class OperationCOPY extends AbstractNFSv4Operation {

    private final static Logger LOGGER = LoggerFactory.getLogger(OperationCOPY.class);

    public OperationCOPY(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_COPY);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException {

        final COPY4res res =  result.opcopy;

        // inter server copy is not supported
        if (_args.opcopy.ca_source_server.length > 0) {
            throw new NotSuppException("Inter-server copy is not supported");
        }

        // only synchronous mode is supported (for now)
        if (!_args.opcopy.ca_consecutive || !_args.opcopy.ca_synchronous) {
            throw new NotSuppException();
        }

        Inode srcInode = context.savedInode();
        Inode dstInode = context.currentInode();

        long srcPos = _args.opcopy.ca_src_offset.value;
        long dstPos = _args.opcopy.ca_dst_offset.value;
        long len = _args.opcopy.ca_count.value;

        NFS4Client client = context.getSession().getClient();

        int srcAccess = context.getStateHandler().getFileTracker().getShareAccess(client, srcInode, _args.opcopy.ca_src_stateid);
        int dstAccess = context.getStateHandler().getFileTracker().getShareAccess(client, dstInode, _args.opcopy.ca_dst_stateid);

        if ((srcAccess & nfs4_prot.OPEN4_SHARE_ACCESS_READ) == 0) {
            throw new OpenModeException("Invalid source inode open mode (required read)");
        }

        if ((dstAccess & nfs4_prot.OPEN4_SHARE_ACCESS_WRITE) == 0) {
            throw new OpenModeException("Invalid destination inode open mode (required write)");
        }

        Future<Long> copyFuture = context.getFs().copyFileRange(srcInode, srcPos, dstInode, dstPos, len);

        try {
            long n = Uninterruptibles.getUninterruptibly(copyFuture);

            res.cr_resok4 = new COPY4resok();
            res.cr_resok4.cr_response = new write_response4();
            res.cr_resok4.cr_response.wr_callback_id = new stateid4[0];
            res.cr_resok4.cr_response.wr_committed = stable_how4.FILE_SYNC4;
            res.cr_resok4.cr_response.wr_count = new length4(n);
            res.cr_resok4.cr_response.wr_writeverf = context.getRebootVerifier();

            res.cr_resok4.cr_requirements = new copy_requirements4();
            res.cr_resok4.cr_requirements.cr_consecutive = true;
            res.cr_resok4.cr_requirements.cr_synchronous = true;

            res.cr_status = nfsstat.NFS_OK;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ChimeraNFSException) {
                res.cr_status = ((ChimeraNFSException) cause).getStatus();
            } else {
                LOGGER.warn("Unexpected error during copyFileRange", e);
                res.cr_status = nfsstat.NFSERR_IO;
            }
        }
    }
}
