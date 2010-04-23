package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.VERIFY4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationVERIFY extends AbstractNFSv4Operation {

        private static final Logger _log = LoggerFactory.getLogger(OperationVERIFY.class);

	OperationVERIFY(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_VERIFY);
	}

	@Override
	public boolean process(CompoundContext context) {

        VERIFY4res res = new VERIFY4res();

        try {

            res.status = nfsstat4.NFS4_OK;

            /*
             * Solaris client work around:
             *
             *  reply OK  in case of empty bit mask
             *
             */

            if( bitSet (_args.opverify.obj_attributes.attrmask ) ) {
                fattr4 currentAttr = OperationGETATTR.getAttributes(_args.opverify.obj_attributes.attrmask, context.currentInode());

                if( Arrays.equals(_args.opverify.obj_attributes.attr_vals.value, currentAttr.attr_vals.value) ) {
                    res.status = nfsstat4.NFS4_OK;
                }else{
                    res.status = nfsstat4.NFS4ERR_NOT_SAME;
                }

            }

            _log.debug("{} is same? {}",
                    new Object[] { context.currentInode().toFullString(), res.status} );

        }catch(ChimeraNFSException he) {
        	res.status = he.getStatus();
        }catch(Exception e){
        	_log.error("VERIFY :", e);
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

       _result.opverify = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}



	private static boolean bitSet( bitmap4 bitmask) {


	    boolean set = false;

	    for( uint32_t mask: bitmask.value ) {

	        if( mask.value != 0 ) {
	            set = true;
	            break;
	        }
	    }

	    return set;
	}
}
