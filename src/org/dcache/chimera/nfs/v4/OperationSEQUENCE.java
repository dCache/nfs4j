package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.sequenceid4;
import org.dcache.chimera.nfs.v4.xdr.sessionid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.slotid4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.SEQUENCE4res;
import org.dcache.chimera.nfs.v4.xdr.SEQUENCE4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;


public class OperationSEQUENCE extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationSEQUENCE.class.getName());
    private final boolean _trackSession;

    public OperationSEQUENCE(FileSystemProvider fs, RpcCall call$, CompoundArgs fh, nfs_argop4 args, boolean trackSession , ExportFile exports) {
        super(fs, exports, call$, fh, args, nfs_opnum4.OP_SEQUENCE);
        _trackSession = trackSession;
    }

    @Override
    public NFSv4OperationResult process() {
       SEQUENCE4res res = new SEQUENCE4res();

        try {
            /*
             *
             * from NFSv4.1 spec:
             *
             * This operation MUST appear as the first operation of any COMPOUND in which it appears.
             * The error NFS4ERR_SEQUENCE_POS will be returned when if it is found in any position in
             * a COMPOUND beyond the first. Operations other than SEQUENCE, BIND_CONN_TO_SESSION,
             * EXCHANGE_ID, CREATE_SESSION, and DESTROY_SESSION, may not appear as the first operation
             * in a COMPOUND. Such operations will get the error NFS4ERR_OP_NOT_IN_SESSION if they do
             * appear at the start of a COMPOUND.
             *
             *
             *
             */


            if(_fh.position() != 0 ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_SEQUENCE_POS, "SEQUENCE not a first operation");
            }

            res.sr_resok4 = new SEQUENCE4resok();

            res.sr_resok4.sr_highest_slotid = new slotid4(_args.opsequence.sa_highest_slotid.value);
            res.sr_resok4.sr_slotid = new slotid4(_args.opsequence.sa_slotid.value);
            res.sr_resok4.sr_target_highest_slotid = new slotid4(_args.opsequence.sa_slotid.value);
            res.sr_resok4.sr_sessionid = new sessionid4(_args.opsequence.sa_sessionid.value);

            NFSv41Session session = NFSv4StateHandler.getInstace().sessionById(_args.opsequence.sa_sessionid.value);

            if(session == null ) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADSESSION, "client not found");
            }

            NFS4Client client = session.getClient();

            if( client.sessionsEmpty(session)) {
                _log.log(Level.FINEST, "no session for id [{0}]",  new String(_args.opsequence.sa_sessionid.value) );
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADSESSION, "client not found");
            }


            /*
             * in some cases we can ignore lease time update,
             * while client do not keep track of them ( DS )
             *
             */
            if( _trackSession ) {
                session.setSlot(_args.opsequence.sa_slotid.value.value,_args.opsequence.sa_sequenceid.value.value );
                session.getClient().updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);
            }
            _fh.setSession(session);

            //res.sr_resok4.sr_sequenceid = new sequenceid4( new uint32_t( session.nextSequenceID()) );
            res.sr_resok4.sr_sequenceid = new sequenceid4( new uint32_t(_args.opsequence.sa_sequenceid.value.value ) );
            res.sr_resok4.sr_status_flags = new uint32_t(0);


            res.sr_status = nfsstat4.NFS4_OK;
        }catch(ChimeraNFSException ne) {
            _log.log(Level.FINEST, "SQUENCE : ", ne.getMessage());
            res.sr_status = ne.getStatus();
        }catch(Exception e) {
               _log.log(Level.SEVERE, "SEQUENCE :", e);
            res.sr_status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

       _result.opsequence = res;

        return new NFSv4OperationResult(_result, res.sr_status);
    }

}
