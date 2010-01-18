package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPEN_CONFIRM4resok;
import org.dcache.chimera.nfs.v4.xdr.OPEN_CONFIRM4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.xdr.RpcCall;
import org.apache.log4j.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;

public class OperationOPEN_CONFIRM extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(OperationOPEN_CONFIRM.class.getName());

	OperationOPEN_CONFIRM(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, ExportFile exports) {
		super(fs, exports, call$, fh, args, nfs_opnum4.OP_OPEN_CONFIRM);
	}

	@Override
	public NFSv4OperationResult process() {


        OPEN_CONFIRM4res res = new OPEN_CONFIRM4res();

        try {

        	FsInode inode = _fh.currentInode();

            if( inode.isDirectory() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_ISDIR, "path is a directory");
            }

            if( inode.isLink() ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "path is a symlink");
            }

        	String stateID = new String( _args.opopen_confirm.open_stateid.other);
        	if(_log.isDebugEnabled() ) {
        		_log.debug("confirmed stateID: " + stateID + " seqid: "  + _args.opopen_confirm.seqid.value.value );
        	}
            Long clientId = NFSv4StateHandler.getInstace().getClientIdByStateId(stateID);
            if(clientId == null ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "bad client id."  );
            }

            NFS4Client client = NFSv4StateHandler.getInstace().getClientByID(clientId);
            if(client == null ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "bad client id."  );
            }

            NFS4State state = client.state(stateID);
            if( state == null) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_STATEID, "bad client id."  );
            }

            state.bumpSeqid();

            if( state.seqid() != _args.opopen_confirm.seqid.value.value ) {
                throw new ChimeraNFSException( nfsstat4.NFS4ERR_BAD_SEQID, "bad seqid."  );
            }

            state.confirm();

            res.resok4 = new OPEN_CONFIRM4resok();
            res.resok4.open_stateid = new  stateid4();
            res.resok4.open_stateid.seqid = new uint32_t( state.seqid() );
            res.resok4.open_stateid.other = state.other();

            res.status = nfsstat4.NFS4_OK;

        }catch(ChimeraNFSException he) {
        	_log.error("open_confirm failed:", he);
            res.status = he.getStatus();
        }catch(Exception e) {
        	_log.error("open_confirm failed:", e);
        	res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

        _result.opopen_confirm = res;

        return new NFSv4OperationResult(_result, res.status);

	}

}
