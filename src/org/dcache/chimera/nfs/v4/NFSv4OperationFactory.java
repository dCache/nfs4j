package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class NFSv4OperationFactory {

	/*
	 * no instance allowed
	 */
	private NFSv4OperationFactory() {}



	public static AbstractNFSv4Operation getOperation(FileSystemProvider fs,
	        RpcCall call$, CompoundArgs fh, nfs_argop4 op,
	                                                   ExportFile exports) {

	        switch ( op.argop ) {
	            case nfs_opnum4.OP_ACCESS:
	            	return new OperationACCESS(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_CLOSE:
	            	return new OperationCLOSE(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_COMMIT:
	            	return new OperationCOMMIT(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_CREATE:
	            	return new OperationCREATE(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_DELEGPURGE:
	            	return new OperationDELEGPURGE(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_DELEGRETURN:
	            	return new OperationDELEGRETURN(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_GETATTR:
	            	return new OperationGETATTR(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_GETFH:
	            	return new OperationGETFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LINK:
	            	return new OperationLINK(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LOCK:
	            	return new OperationLOCK(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LOCKT:
	            	return new OperationLOCKT(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LOCKU:
	            	return new OperationLOCKU(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LOOKUP:
	            	return new OperationLOOKUP(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LOOKUPP:
	            	return new OperationLOOKUPP(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_NVERIFY:
	            	return new OperationNVERIFY(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_OPEN:
	            	return new OperationOPEN(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_OPENATTR:
	            	return new OperationOPENATTR(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_OPEN_CONFIRM:
	            	return new OperationOPEN_CONFIRM(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_OPEN_DOWNGRADE:break;
	            case nfs_opnum4.OP_PUTFH:
	            	return new OperationPUTFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_PUTPUBFH:
	            	return new OperationPUTPUBFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_PUTROOTFH:
	            	return new OperationPUTROOTFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_READ:
	            	return new OperationREAD(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_READDIR:
	            	return new OperationREADDIR(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_READLINK:
	            	return new OperationREADLINK(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_REMOVE:
	            	return new OperationREMOVE(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_RENAME:
	            	return new OperationRENAME(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_RENEW:
	            	return new OperationRENEW(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_RESTOREFH:
	            	return new OperationRESTOREFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SAVEFH:
	            	return new OperationSAVEFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SECINFO:
	            	return new OperationSECINFO(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SETATTR:
	            	return new OperationSETATTR(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SETCLIENTID:
	            	return new OperationSETCLIENTID(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SETCLIENTID_CONFIRM:
	            	return new OperationSETCLIENTID_CONFIRM(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_VERIFY:
	            	return new OperationVERIFY(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_WRITE:
	            	return new OperationWRITE(fs, call$, fh, op, exports);
	/*            case nfs_opnum4.OP_RELEASE_LOCKOWNER:
	            	nRes.oprelease_lockowner = new RELEASE_LOCKOWNER4res();
	            	res = new NFSv4OperationResult(nRes, nfsstat4.NFS4ERR_NOTSUPP);
	                break; */

	            /**
	             *  NFSv4.1 (pNFS)
	             */

	            case nfs_opnum4.OP_GETDEVICELIST:
	            	return new OperationGETDEVICELIST(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LAYOUTGET:
	            	return new OperationLAYOUTGET(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LAYOUTCOMMIT:
	            	return new OperationLAYOUTCOMMIT(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_LAYOUTRETURN:
	            	return new OperationLAYOUTRETURN(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_GETDEVICEINFO:
	            	return new OperationGETDEVICEINFO(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_EXCHANGE_ID:
	            	return new OperationEXCHANGE_ID(fs, call$, fh, op, nfs4_prot.EXCHGID4_FLAG_USE_PNFS_MDS, exports);
	            case nfs_opnum4.OP_CREATE_SESSION:
	            	return new OperationCREATE_SESSION(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_DESTROY_SESSION:
	            	return new OperationDESTROY_SESSION(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SEQUENCE:
	            	return new OperationSEQUENCE(fs, call$, fh, op, true, exports);
	            case nfs_opnum4.OP_ILLEGAL:

	            }


	        return new OperationILLEGAL(fs, call$, fh, op, exports);
	    }


}
