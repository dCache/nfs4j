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
package org.dcache.chimera.nfs.v4.ds;

import org.dcache.chimera.nfs.v4.*;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.vfs.FsCache;

public class DSOperationFactory implements NFSv4OperationFactory {

    private final FsCache _fs;

    public DSOperationFactory(FsCache fs) {
        _fs = fs;
    }

    @Override
    public AbstractNFSv4Operation getOperation(nfs_argop4 op) {

        switch (op.argop) {
            case nfs_opnum4.OP_COMMIT:
                return new OperationCOMMIT(op);
            case nfs_opnum4.OP_GETATTR:
                return new OperationGETATTR(op);
            case nfs_opnum4.OP_PUTFH:
                return new OperationPUTFH(op);
            case nfs_opnum4.OP_PUTROOTFH:
                return new OperationPUTROOTFH(op);
            case nfs_opnum4.OP_READ:
                return new DSOperationREAD(op, _fs);
            case nfs_opnum4.OP_WRITE:
                return new DSOperationWRITE(op, _fs);
            case nfs_opnum4.OP_EXCHANGE_ID:
                return new OperationEXCHANGE_ID(op, nfs4_prot.EXCHGID4_FLAG_USE_PNFS_DS);
            case nfs_opnum4.OP_CREATE_SESSION:
                return new OperationCREATE_SESSION(op);
            case nfs_opnum4.OP_DESTROY_SESSION:
                return new OperationDESTROY_SESSION(op);
            case nfs_opnum4.OP_SEQUENCE:
                return new OperationSEQUENCE(op);
            case nfs_opnum4.OP_RECLAIM_COMPLETE:
                return new OperationRECLAIM_COMPLETE(op);
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION:
                return new OperationBIND_CONN_TO_SESSION(op);
            case nfs_opnum4.OP_DESTROY_CLIENTID:
                return new OperationDESTROY_CLIENTID(op);
        }

        return new OperationILLEGAL(op);
    }
}
