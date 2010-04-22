package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OperationLAYOUTGET extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationLAYOUTGET.class.getName());

    OperationLAYOUTGET(nfs_argop4 args) {
    super(args, nfs_opnum4.OP_LAYOUTGET);
    }

    @Override
    public boolean process(CompoundContext context) {

    LAYOUTGET4res res = new LAYOUTGET4res();

    try {

        /*
         * dCache supports FILE layout
         *
         * LAYOUTGET is used by a client to get a layout for a file
         */

        /*
         * DRAFT-17: To get a layout from a specific offset through the
         * end-of-file, regardless of the file's length, a loga_length field
         * with all bits set to 1 (one) should be used
         */
        length4 lengthRange = new length4();
        lengthRange.value = new uint64_t(0xffffffff);

        if (_args.oplayoutget.loga_offset.value.value != 0) {
            if (_args.oplayoutget.loga_length.value.value == 0) {
                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "length == 0");
            }

            /*
             * FIXME: sing/unsign issue here
             */
//            if ((_args.oplayoutget.loga_length.value.value + _args.oplayoutget.loga_offset.value.value) > lengthRange.value.value) {
//                throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "offset+length too big");
//            }
        }

        if ( !(_args.oplayoutget.loga_iomode  == layoutiomode4.LAYOUTIOMODE4_RW ||
				_args.oplayoutget.loga_iomode  == layoutiomode4.LAYOUTIOMODE4_READ)) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADIOMODE, "invalid loga_iomode");
        }

        if (_args.oplayoutget.loga_layout_type > 3) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADLAYOUT, "layouts supported but no matching found ("+ _args.oplayoutget.loga_layout_type +")");
        } else if (_args.oplayoutget.loga_layout_type != layouttype4.LAYOUT4_NFSV4_1_FILES) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_LAYOUTUNAVAILABLE, "layout not supported");
        }

        if (_args.oplayoutget.loga_minlength.value.value < 1) {
               throw new ChimeraNFSException(nfsstat4.NFS4ERR_BADLAYOUT, "loga_minlength field should be at least one.");
        }

        res.logr_resok4 = new LAYOUTGET4resok();

        Layout ioLayout = context.getDeviceManager().layoutGet(context.currentInode(),
                _args.oplayoutget.loga_iomode,
                context.getSession().getClient(),
                _args.oplayoutget.loga_stateid);

        res.logr_resok4.logr_layout = ioLayout.getLayoutSegments();
        res.logr_resok4.logr_stateid = ioLayout.getStateid();
        res.logr_resok4.logr_return_on_close = ioLayout.returnOnClose();

        res.logr_status = nfsstat4.NFS4_OK;

    } catch (ChimeraNFSException he) {
        _log.log(Level.INFO, "LAYOUTGET: {0}",  he.getMessage());
        res.logr_status = he.getStatus();
    } catch (Exception e) {
        _log.log(Level.SEVERE, "LAYOUTGET:", e);
        res.logr_status = nfsstat4.NFS4ERR_SERVERFAULT;
    }

    _result.oplayoutget = res;

        context.processedOperations().add(_result);
        return res.logr_status == nfsstat4.NFS4_OK;

    }

}
