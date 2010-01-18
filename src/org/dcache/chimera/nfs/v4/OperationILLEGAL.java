package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.ILLEGAL4res;
import org.apache.log4j.Logger;

public class OperationILLEGAL extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationILLEGAL.class.getName());

	public OperationILLEGAL(nfs_argop4 op) {
		super(op, nfs_opnum4.OP_ILLEGAL);
	}

    @Override
    public boolean process(CompoundContext context) {

        _log.info("ILLEGAL OPCODE:" + _args.argop);

        ILLEGAL4res opResILLEGAL = new ILLEGAL4res();
        opResILLEGAL.status = nfsstat4.NFS4ERR_OP_ILLEGAL;
        _result.opillegal = opResILLEGAL;
        context.processedOperations().add(_result);
        return false;
    }

}
