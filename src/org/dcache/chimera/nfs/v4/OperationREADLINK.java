package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.linktext4;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.utf8string;
import org.dcache.chimera.nfs.v4.xdr.READLINK4res;
import org.dcache.chimera.nfs.v4.xdr.READLINK4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.apache.log4j.Logger;

public class OperationREADLINK extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationREADLINK.class.getName());

	OperationREADLINK(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_READLINK);
	}

	@Override
	public boolean process(CompoundContext context) {
        READLINK4res res = new READLINK4res();

        try {

        	if( !context.currentInode().isLink()  ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "not a symlink");
        	}

            byte[] link = context.currentInode().readlink();
            _log.debug("NFS Request  READLINK4 link: " + new String(link) );
            res.resok4 = new READLINK4resok();
            res.resok4.link = new linktext4();
            res.resok4.link.value = new utf8str_cs();
            res.resok4.link.value.value = new utf8string();
            res.resok4.link.value.value.value = link;
            res.status = nfsstat4.NFS4_OK;

        }catch(ChimeraNFSException he){
        	if(_log.isDebugEnabled() ) {
        		_log.debug("READLINK: " + he.getMessage() );
        	}
            res.status = he.getStatus();
        }catch(Exception e) {
            _log.error("READLINK4", e);
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

      _result.opreadlink = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}

}
