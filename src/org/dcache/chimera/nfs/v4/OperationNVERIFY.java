package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.NVERIFY4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationNVERIFY extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationNVERIFY.class);

    OperationNVERIFY(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_NVERIFY);
    }

    @Override
    public boolean process(CompoundContext context) {

        NVERIFY4res res = new NVERIFY4res();

        try {

            fattr4 currentAttr = OperationGETATTR.getAttributes(_args.opnverify.obj_attributes.attrmask, context.currentInode());

            res.status = nfsstat4.NFS4ERR_SAME;

            for (int i = 0; i < _args.opnverify.obj_attributes.attr_vals.value.length; i++) {

                if (_args.opnverify.obj_attributes.attr_vals.value[i] != currentAttr.attr_vals.value[i]) {
                    res.status = nfsstat4.NFS4_OK;
                    break;
                }
            }

            _log.debug("{} is !same = {}",
                    new Object[]{context.currentInode().toFullString(), res.status});
        } catch (ChimeraNFSException he) {
            res.status = he.getStatus();
        } catch (Exception e) {
            _log.error("NVERIFY :", e);
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }

        _result.opnverify = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;

    }
}
