package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.NVERIFY4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;

public class OperationNVERIFY extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationNVERIFY.class.getName());

	OperationNVERIFY(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_NVERIFY);
	}

	@Override
	public NFSv4OperationResult process() {

	       NVERIFY4res res = new NVERIFY4res();

	        try {

	            fattr4 currentAttr = OperationGETATTR.getAttributes(_args.opnverify.obj_attributes.attrmask, _fh.currentInode());

	            res.status = nfsstat4.NFS4ERR_SAME;

	            for(int i = 0; i < _args.opnverify.obj_attributes.attr_vals.value.length; i++) {

	                if( _args.opnverify.obj_attributes.attr_vals.value[i] != currentAttr.attr_vals.value[i] ) {
	                    res.status = nfsstat4.NFS4_OK;
	                    break;
	                }
	            }

                _log.log(Level.FINEST, "{0} is !same = {1}",
                        new Object[] {_fh.currentInode().toFullString(), res.status}
                );
            }catch(ChimeraNFSException he) {
	        	res.status = he.getStatus();
	        }catch(Exception e){
                _log.log(Level.SEVERE, "NVERIFY :", e);
	            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
	        }

	        _result.opnverify = res;

	        return new NFSv4OperationResult(_result, res.status);
	}
}
