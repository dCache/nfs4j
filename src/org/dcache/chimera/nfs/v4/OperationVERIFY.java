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

import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationVERIFY extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationVERIFY.class.getName());

	OperationVERIFY(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_VERIFY);
	}

	@Override
	public NFSv4OperationResult process() {

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
                fattr4 currentAttr = OperationGETATTR.getAttributes(_args.opverify.obj_attributes.attrmask, _fh.currentInode());

                if( Arrays.equals(_args.opverify.obj_attributes.attr_vals.value, currentAttr.attr_vals.value) ) {
                    res.status = nfsstat4.NFS4_OK;
                }else{
                    res.status = nfsstat4.NFS4ERR_NOT_SAME;
                }

            }

            if(_log.isDebugEnabled() ) {
            	_log.debug(_fh.currentInode().toFullString() + " is same? = " + res.status );
            }

        }catch(ChimeraNFSException he) {
        	res.status = he.getStatus();
        }catch(Exception e){
        	_log.error("VERIFY :", e);
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

       _result.opverify = res;

        return new NFSv4OperationResult(_result, res.status);
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
