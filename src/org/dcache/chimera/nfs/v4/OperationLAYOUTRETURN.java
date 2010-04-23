package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.layoutreturn_type4;
import org.dcache.chimera.nfs.v4.xdr.layoutreturn_stateid;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LAYOUTRETURN4res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLAYOUTRETURN extends AbstractNFSv4Operation {

        private static final Logger _log = LoggerFactory.getLogger(OperationLAYOUTRETURN.class);

	OperationLAYOUTRETURN(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_LAYOUTRETURN);
	}

	@Override
	public boolean process(CompoundContext context) {

	    LAYOUTRETURN4res res = new LAYOUTRETURN4res();

        _log.debug("LAYOUTRETURN4args :        type: {}", _args.oplayoutreturn.lora_layout_type);
        _log.debug("LAYOUTRETURN4args :        mode: {}", _args.oplayoutreturn.lora_iomode);
        _log.debug("LAYOUTRETURN4args : return type: {}", _args.oplayoutreturn.lora_layoutreturn.lr_returntype);
        _log.debug("LAYOUTRETURN4args :     reclaim: {}", _args.oplayoutreturn.lora_reclaim);

        try {

                if( _args.oplayoutreturn.lora_layoutreturn.lr_returntype == layoutreturn_type4.LAYOUTRETURN4_FILE) {

                context.getDeviceManager().
                        layoutReturn(context.getSession().getClient(),
                                _args.oplayoutreturn.lora_layoutreturn.lr_layout.lrf_stateid);
    		}

        	res.lorr_stateid = new layoutreturn_stateid();
        	res.lorr_stateid.lrs_present = false;
        	res.lorr_stateid.lrs_stateid = new stateid4();
        	res.lorr_stateid.lrs_stateid.seqid = new uint32_t(0);
        	res.lorr_stateid.lrs_stateid.other = new byte[12];

        	res.lorr_status = nfsstat4.NFS4_OK;

        }catch(Exception e) {
            res.lorr_status = nfsstat4.NFS4ERR_SERVERFAULT;
            _log.error("LAYOUTRETURN: ", e);
        }
      _result.oplayoutreturn = res;

            context.processedOperations().add(_result);
            return res.lorr_status == nfsstat4.NFS4_OK;
	}

}
