/*
 * Copyright (c) 2009 - 2026 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.*;

public class MDSOperationExecutor extends AbstractOperationExecutor {


    /*
     * The operations are pre-initialized to reuse the instances.
     */

    private final AbstractNFSv4Operation opAccess = new OperationACCESS();
    private final AbstractNFSv4Operation opClose = new OperationCLOSE();
    private final AbstractNFSv4Operation opCommit = new OperationCOMMIT();
    private final AbstractNFSv4Operation opCreate = new OperationCREATE();
    private final AbstractNFSv4Operation opDelegPurge = new OperationDELEGPURGE();
    private final AbstractNFSv4Operation opDelegReturn = new OperationDELEGRETURN();
    private final AbstractNFSv4Operation opGetAttr = new OperationGETATTR();
    private final AbstractNFSv4Operation opGetFH = new OperationGETFH();
    private final AbstractNFSv4Operation opLink = new OperationLINK();
    private final AbstractNFSv4Operation opLock = new OperationLOCK();
    private final AbstractNFSv4Operation opLockT = new OperationLOCKT();
    private final AbstractNFSv4Operation opLockU = new OperationLOCKU();
    private final AbstractNFSv4Operation opLookup = new OperationLOOKUP();
    private final AbstractNFSv4Operation opLookupP = new OperationLOOKUPP();
    private final AbstractNFSv4Operation opNVerify = new OperationNVERIFY();
    private final AbstractNFSv4Operation opOpen = new OperationOPEN();
    private final AbstractNFSv4Operation opOpenAttr = new OperationOPENATTR();
    private final AbstractNFSv4Operation opOpenConfirm = new OperationOPEN_CONFIRM();
    private final AbstractNFSv4Operation opOpenDowngrade = new OperationOPEN_DOWNGRADE();
    private final AbstractNFSv4Operation opPutFH = new OperationPUTFH();
    private final AbstractNFSv4Operation opPutPubFH = new OperationPUTPUBFH();
    private final AbstractNFSv4Operation opPutRootFH = new OperationPUTROOTFH();
    private final AbstractNFSv4Operation opRead = new OperationREAD();
    private final AbstractNFSv4Operation opReadDir = new OperationREADDIR();
    private final AbstractNFSv4Operation opReadLink = new OperationREADLINK();
    private final AbstractNFSv4Operation opRemove = new OperationREMOVE();
    private final AbstractNFSv4Operation opRename = new OperationRENAME();
    private final AbstractNFSv4Operation opRenew = new OperationRENEW();
    private final AbstractNFSv4Operation opRestoreFH = new OperationRESTOREFH();
    private final AbstractNFSv4Operation opSaveFH = new OperationSAVEFH();
    private final AbstractNFSv4Operation opSecInfo = new OperationSECINFO();
    private final AbstractNFSv4Operation opSetAttr = new OperationSETATTR();
    private final AbstractNFSv4Operation opSetClientId = new OperationSETCLIENTID();
    private final AbstractNFSv4Operation opSetClientIdConfirm = new OperationSETCLIENTID_CONFIRM();
    private final AbstractNFSv4Operation opVerify = new OperationVERIFY();
    private final AbstractNFSv4Operation opWrite = new OperationWRITE();
    private final AbstractNFSv4Operation opReleaseLockOwner = new OperationRELEASE_LOCKOWNER();
    private final AbstractNFSv4Operation opBackChannelCtl = new OperationBACKCHANNEL_CTL();
    private final AbstractNFSv4Operation opGetDeviceList = new OperationGETDEVICELIST();
    private final AbstractNFSv4Operation opLayoutError = new OperationLAYOUTERROR();
    private final AbstractNFSv4Operation opLayoutGet = new OperationLAYOUTGET();
    private final AbstractNFSv4Operation opLayoutCommit = new OperationLAYOUTCOMMIT();
    private final AbstractNFSv4Operation opLayoutReturn = new OperationLAYOUTRETURN();
    private final AbstractNFSv4Operation opLayoutStats = new OperationLAYOUTSTATS();
    private final AbstractNFSv4Operation opGetDeviceInfo = new OperationGETDEVICEINFO();
    private final AbstractNFSv4Operation opExchangeId = new OperationEXCHANGE_ID();
    private final AbstractNFSv4Operation opCreateSession = new OperationCREATE_SESSION();
    private final AbstractNFSv4Operation opDestroySession = new OperationDESTROY_SESSION();
    private final AbstractNFSv4Operation opSequence = new OperationSEQUENCE();
    private final AbstractNFSv4Operation opDestroyClientId = new OperationDESTROY_CLIENTID();
    private final AbstractNFSv4Operation opReclaimComplete = new OperationRECLAIM_COMPLETE();
    private final AbstractNFSv4Operation opBindConnToSession = new OperationBIND_CONN_TO_SESSION();
    private final AbstractNFSv4Operation opSecInfoNoName = new OperationSECINFO_NO_NAME();
    private final AbstractNFSv4Operation opFreeStateId = new OperationFREE_STATEID();
    private final AbstractNFSv4Operation opTestStateId = new OperationTEST_STATEID();
    private final AbstractNFSv4Operation opGetXAttr = new OperationGETXATTR();
    private final AbstractNFSv4Operation opSetXAttr = new OperationSETXATTR();
    private final AbstractNFSv4Operation opListXAttr = new OperationLISTXATTRS();
    private final AbstractNFSv4Operation opRemoveXAttr = new OperationREMOVEXATTR();
    private final AbstractNFSv4Operation opCopy = new OperationCOPY();
    private final AbstractNFSv4Operation opIllegal = new OperationILLEGAL();

    /*
     * The is no Unsupported Operations, thus create one.
     */
    private final AbstractNFSv4Operation opUnsupported = new AbstractNFSv4Operation() {
        @Override
        public void process(CompoundContext context, nfs_argop4 args, nfs_resop4 result) {
            result.setStatus(nfsstat.NFSERR_NOTSUPP);

        }
    };


    @Override
    protected AbstractNFSv4Operation getOperation(nfs_argop4 op) {

        return switch (op.argop) {
            case nfs_opnum4.OP_ACCESS -> opAccess;
            case nfs_opnum4.OP_CLOSE -> opClose;
            case nfs_opnum4.OP_COMMIT -> opCommit;
            case nfs_opnum4.OP_CREATE -> opCreate;
            case nfs_opnum4.OP_DELEGPURGE -> opDelegPurge;
            case nfs_opnum4.OP_DELEGRETURN -> opDelegReturn;
            case nfs_opnum4.OP_GETATTR -> opGetAttr;
            case nfs_opnum4.OP_GETFH -> opGetFH;
            case nfs_opnum4.OP_LINK -> opLink;
            case nfs_opnum4.OP_LOCK -> opLock;
            case nfs_opnum4.OP_LOCKT -> opLockT;
            case nfs_opnum4.OP_LOCKU -> opLockU;
            case nfs_opnum4.OP_LOOKUP -> opLookup;
            case nfs_opnum4.OP_LOOKUPP -> opLookupP;
            case nfs_opnum4.OP_NVERIFY -> opNVerify;
            case nfs_opnum4.OP_OPEN -> opOpen;
            case nfs_opnum4.OP_OPENATTR -> opOpenAttr;
            case nfs_opnum4.OP_OPEN_CONFIRM -> opOpenConfirm;
            case nfs_opnum4.OP_OPEN_DOWNGRADE -> opOpenDowngrade;
            case nfs_opnum4.OP_PUTFH -> opPutFH;
            case nfs_opnum4.OP_PUTPUBFH -> opPutPubFH;
            case nfs_opnum4.OP_PUTROOTFH -> opPutRootFH;
            case nfs_opnum4.OP_READ -> opRead;
            case nfs_opnum4.OP_READDIR -> opReadDir;
            case nfs_opnum4.OP_READLINK -> opReadLink;
            case nfs_opnum4.OP_REMOVE -> opRemove;
            case nfs_opnum4.OP_RENAME -> opRename;
            case nfs_opnum4.OP_RENEW -> opRenew;
            case nfs_opnum4.OP_RESTOREFH -> opRestoreFH;
            case nfs_opnum4.OP_SAVEFH -> opSaveFH;
            case nfs_opnum4.OP_SECINFO -> opSecInfo;
            case nfs_opnum4.OP_SETATTR -> opSetAttr;
            case nfs_opnum4.OP_SETCLIENTID -> opSetClientId;
            case nfs_opnum4.OP_SETCLIENTID_CONFIRM -> opSetClientIdConfirm;
            case nfs_opnum4.OP_VERIFY -> opVerify;
            case nfs_opnum4.OP_WRITE -> opWrite;
            case nfs_opnum4.OP_RELEASE_LOCKOWNER -> opReleaseLockOwner;
            case nfs_opnum4.OP_BACKCHANNEL_CTL -> opBackChannelCtl;


            /*
             * NFSv4.1 (pNFS)
             */
            case nfs_opnum4.OP_GETDEVICELIST -> opGetDeviceList;
            case nfs_opnum4.OP_LAYOUTERROR -> opLayoutError;
            case nfs_opnum4.OP_LAYOUTGET -> opLayoutGet;
            case nfs_opnum4.OP_LAYOUTCOMMIT -> opLayoutCommit;
            case nfs_opnum4.OP_LAYOUTRETURN -> opLayoutReturn;
            case nfs_opnum4.OP_LAYOUTSTATS -> opLayoutStats;
            case nfs_opnum4.OP_GETDEVICEINFO -> opGetDeviceInfo;
            case nfs_opnum4.OP_EXCHANGE_ID -> opExchangeId;
            case nfs_opnum4.OP_CREATE_SESSION -> opCreateSession;
            case nfs_opnum4.OP_DESTROY_SESSION -> opDestroySession;
            case nfs_opnum4.OP_SEQUENCE -> opSequence;
            case nfs_opnum4.OP_DESTROY_CLIENTID -> opDestroyClientId;
            case nfs_opnum4.OP_RECLAIM_COMPLETE -> opReclaimComplete;
            case nfs_opnum4.OP_BIND_CONN_TO_SESSION -> opBindConnToSession;
            case nfs_opnum4.OP_SECINFO_NO_NAME -> opSecInfoNoName;
            case nfs_opnum4.OP_FREE_STATEID -> opFreeStateId;
            case nfs_opnum4.OP_TEST_STATEID -> opTestStateId;

            /*
             * xattr extension
             */
            case nfs_opnum4.OP_GETXATTR -> opGetXAttr;
            case nfs_opnum4.OP_SETXATTR -> opSetXAttr;
            case nfs_opnum4.OP_LISTXATTRS -> opListXAttr;
            case nfs_opnum4.OP_REMOVEXATTR -> opRemoveXAttr;

            /*
             * NFSv4.2
             */
            case nfs_opnum4.OP_COPY -> opCopy;

            // legit, but not supported by nfs4j operations.
            case nfs_opnum4.OP_ALLOCATE, nfs_opnum4.OP_COPY_NOTIFY, nfs_opnum4.OP_DEALLOCATE,
                 nfs_opnum4.OP_IO_ADVISE, nfs_opnum4.OP_OFFLOAD_CANCEL,
                 nfs_opnum4.OP_OFFLOAD_STATUS, nfs_opnum4.OP_READ_PLUS, nfs_opnum4.OP_SEEK,
                 nfs_opnum4.OP_WRITE_SAME, nfs_opnum4.OP_CLONE, nfs_opnum4.OP_GET_DIR_DELEGATION ->
                  opUnsupported;

            default -> opIllegal;

        };
    }
}
