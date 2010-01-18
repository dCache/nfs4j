package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.length4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.newsize4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LAYOUTCOMMIT4resok;
import org.dcache.chimera.nfs.v4.xdr.LAYOUTCOMMIT4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.apache.log4j.Logger;
import org.dcache.chimera.ChimeraFsException;

public class OperationLAYOUTCOMMIT extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationLAYOUTCOMMIT.class.getName());

	OperationLAYOUTCOMMIT(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_LAYOUTCOMMIT);
	}

	@Override
	public boolean process(CompoundContext context) {

    	LAYOUTCOMMIT4res res = new LAYOUTCOMMIT4res();

    	try {

	    	if(_log.isDebugEnabled() ) {

	    		_log.debug("LAYOUTCOMMIT: inode=" + context.currentInode().toFullString() + " length="
	    				+  _args.oplayoutcommit.loca_length.value.value + " offset="
	    				+ _args.oplayoutcommit.loca_offset.value.value + " loca_last_write_offset="
	    				+ ( _args.oplayoutcommit.loca_last_write_offset.no_newoffset ?
	    						_args.oplayoutcommit.loca_last_write_offset.no_offset.value : "notset")
	    				);
	    	}

	    	if( _args.oplayoutcommit.loca_length.value.value > 0 ) {
	    		context.getFs().setFileSize(context.currentInode(), _args.oplayoutcommit.loca_length.value.value);
	    	}


	    	res.locr_resok4 = new LAYOUTCOMMIT4resok();
	    	res.locr_resok4.locr_newsize = new newsize4();
	    	res.locr_resok4.locr_newsize.ns_sizechanged = true;
	    	res.locr_resok4.locr_newsize.ns_size = new length4(new uint64_t( context.currentInode().statCache().getSize() ) );
	    	res.locr_status = nfsstat4.NFS4_OK;



        }catch(ChimeraNFSException hne) {
    		res.locr_status = hne.getStatus();
    	}catch(ChimeraFsException hfe) {
    		_log.error("LAYOUTCOMMIT:", hfe);
    		res.locr_status = nfsstat4.NFS4ERR_SERVERFAULT;
    	}catch(Exception e) {
        	_log.error("LAYOUTCOMMIT:", e);
            res.locr_status = nfsstat4.NFS4ERR_SERVERFAULT;
    	}

        _result.oplayoutcommit = res;

            context.processedOperations().add(_result);
            return res.locr_status == nfsstat4.NFS4_OK;

	}

}
