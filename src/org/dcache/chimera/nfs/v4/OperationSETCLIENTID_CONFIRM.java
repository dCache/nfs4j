package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.SETCLIENTID_CONFIRM4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationSETCLIENTID_CONFIRM extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationPUTFH.class);

    OperationSETCLIENTID_CONFIRM(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_SETCLIENTID_CONFIRM);
    }

    @Override
    public boolean process(CompoundContext context) {

        SETCLIENTID_CONFIRM4res res = new SETCLIENTID_CONFIRM4res();

        try {
            Long clientid = Long.valueOf(_args.opsetclientid_confirm.clientid.value.value);

            NFS4Client client = context.getStateHandler().getClientByID(clientid);
            if (client == null) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_STALE_CLIENTID, "Bad client id");
            }

            res.status = nfsstat4.NFS4ERR_INVAL;
            if (client.verify_serverId(_args.opsetclientid_confirm.clientid.value.value) && client.verify_verifier(_args.opsetclientid_confirm.setclientid_confirm.value)) {
                res.status = nfsstat4.NFS4_OK;
                client.confirmed();
            }
        } catch (ChimeraNFSException he) {
            _log.debug("SETCLIENTID_CONFIRM: {}", he.getMessage());
            res.status = he.getStatus();
        }

        _result.opsetclientid_confirm = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;
    }
}
