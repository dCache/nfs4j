package org.dcache.chimera.nfs.v4.mover;

import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v4.AbstractNFSv4Operation;
import org.dcache.chimera.nfs.v4.CompoundArgs;
import org.dcache.chimera.nfs.v4.OperationCOMMIT;
import org.dcache.chimera.nfs.v4.OperationCREATE_SESSION;
import org.dcache.chimera.nfs.v4.OperationDESTROY_SESSION;
import org.dcache.chimera.nfs.v4.OperationEXCHANGE_ID;
import org.dcache.chimera.nfs.v4.OperationGETATTR;
import org.dcache.chimera.nfs.v4.OperationILLEGAL;
import org.dcache.chimera.nfs.v4.OperationPUTFH;
import org.dcache.chimera.nfs.v4.OperationPUTROOTFH;
import org.dcache.chimera.nfs.v4.OperationSEQUENCE;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.xdr.RpcCall;

class DSNFSv4OperationFactory {

	/*
	 * no instance allowed
	 */
	private DSNFSv4OperationFactory() {}



	static AbstractNFSv4Operation getOperation(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 op, ExportFile exports) {

	        switch ( op.argop ) {
	            case nfs_opnum4.OP_COMMIT:
	            	return new OperationCOMMIT(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_GETATTR:
	            	return new OperationGETATTR(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_PUTFH:
	            	return new OperationPUTFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_PUTROOTFH:
	            	return new OperationPUTROOTFH(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_READ:
	            	return new DSOperationREAD(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_WRITE:
	            	return new DSOperationWRITE(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_EXCHANGE_ID:
	            	return new OperationEXCHANGE_ID(fs, call$, fh, op, nfs4_prot.EXCHGID4_FLAG_USE_PNFS_DS, exports);
	            case nfs_opnum4.OP_CREATE_SESSION:
	            	return new OperationCREATE_SESSION(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_DESTROY_SESSION:
	            	return new OperationDESTROY_SESSION(fs, call$, fh, op, exports);
	            case nfs_opnum4.OP_SEQUENCE:
	            	return new OperationSEQUENCE(fs, call$, fh, op, false, exports);
	            case nfs_opnum4.OP_ILLEGAL:

	            }


	        return new OperationILLEGAL(fs, call$, fh, op, exports);
	    }


}
