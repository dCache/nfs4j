package org.dcache.chimera.nfs.v4.mover;

import org.dcache.chimera.nfs.v4.AbstractNFSv4Operation;
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

class DSNFSv4OperationFactory {

	/*
	 * no instance allowed
	 */
	private DSNFSv4OperationFactory() {}



	static AbstractNFSv4Operation getOperation(nfs_argop4 op) {

	        switch ( op.argop ) {
	            case nfs_opnum4.OP_COMMIT:
	            	return new OperationCOMMIT(op);
	            case nfs_opnum4.OP_GETATTR:
	            	return new OperationGETATTR(op);
	            case nfs_opnum4.OP_PUTFH:
	            	return new OperationPUTFH(op);
	            case nfs_opnum4.OP_PUTROOTFH:
	            	return new OperationPUTROOTFH(op);
	            case nfs_opnum4.OP_READ:
	            	return new DSOperationREAD(op);
	            case nfs_opnum4.OP_WRITE:
	            	return new DSOperationWRITE(op);
	            case nfs_opnum4.OP_EXCHANGE_ID:
	            	return new OperationEXCHANGE_ID(op, nfs4_prot.EXCHGID4_FLAG_USE_PNFS_DS);
	            case nfs_opnum4.OP_CREATE_SESSION:
	            	return new OperationCREATE_SESSION(op);
	            case nfs_opnum4.OP_DESTROY_SESSION:
	            	return new OperationDESTROY_SESSION(op);
	            case nfs_opnum4.OP_SEQUENCE:
	            	return new OperationSEQUENCE(op, false);
	            case nfs_opnum4.OP_ILLEGAL:

	            }


	        return new OperationILLEGAL(op);
	    }


}
