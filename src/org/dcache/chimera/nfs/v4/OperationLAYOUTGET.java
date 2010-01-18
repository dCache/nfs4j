package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.layout_content4;
import org.dcache.chimera.nfs.v4.xdr.device_addr4;
import org.dcache.chimera.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_fh4;
import org.dcache.chimera.nfs.v4.xdr.offset4;
import org.dcache.chimera.nfs.v4.xdr.nfl_util4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.layouttype4;
import org.dcache.chimera.nfs.v4.xdr.layout4;
import org.dcache.chimera.nfs.v4.xdr.layoutiomode4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.length4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.LAYOUTGET4res;
import org.dcache.chimera.nfs.v4.xdr.LAYOUTGET4resok;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.chimera.FsInodeType;

import org.dcache.xdr.XdrBuffer;

public class OperationLAYOUTGET extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationLAYOUTGET.class.getName());

    static final DeviceID MSD_ID = DeviceID.valueOf(0);

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

        NFS4IoDevice ioDevice = null;
        if (context.currentInode().type() == FsInodeType.INODE ) {
            ioDevice = NFSv41DeviceManagerFactory.getDeviceManager().getIoDevice(
                    context.currentInode(),
                    _args.oplayoutget.loga_iomode,
                    context.getRpcCall().getTransport().getRemoteSocketAddress().getAddress(),
                    _args.oplayoutget.loga_stateid);
        }else{

            InetSocketAddress[] addresses = new InetSocketAddress[1];
            addresses[0] = context.getRpcCall().getTransport().getLocalSocketAddress();

            device_addr4 deviceAddr = DeviceManager.deviceAddrOf( addresses );
            ioDevice = new NFS4IoDevice(MSD_ID, deviceAddr);

            NFSv41DeviceManagerFactory.getDeviceManager().
                    addIoDevice(ioDevice, layoutiomode4.LAYOUTIOMODE4_ANY);
        }

        _log.log(Level.FINER, "LAYOUT for {0} sd# {1}",
                new Object[] { context.currentInode().toFullString(),
                    ioDevice.getDeviceId()}
        );

        /*
         * The nfsv4_1_file_layout4 data type represents the layout. It is
         * composed of four fields: 1. nfl_deviceid: The device ID which
         * maps to a value of type nfsv4_1_file_layout_ds_addr4. 2.
         * nfl_util: Like the nflh_util field of data type
         * nfsv4_1_file_layouthint4, a compact representation of how the
         * data on a file on each data server is packed, whether the client
         * should send COMMIT operations to the metadata server or data
         * server, and the stripe unit size. 3. nfl_first_stripe_index: The
         * index into the first element of the nfla_stripe_indices array to
         * use. 4. nfl_fh_list. An array of data server filehandles for each
         * list of data servers in each element of the
         * nflda_multipath_ds_list array. The number of elements in
         * nfl_fh_list MUST be of three values: Zero. This means that
         * filehandles used for each data server are the same as the
         * filehandle returned by the OPEN operation from the metadata
         * server. One. This means that every data server uses the same
         * filehandle: what is specified in nfl_fh_list[0]. The stripe
         * count, i.e. the number of elements as nflda_multipath_ds_list.
         * When issuing an I/O to any data server in
         * nfla_multipath_ds_list[X], the filehandle in nfl_fh_list[X] MUST
         * be used.
         */

        nfsv4_1_file_layout4 layout = new nfsv4_1_file_layout4();

        layout.nfl_deviceid = ioDevice.getDeviceId().toDeviceid4();
        layout.nfl_fh_list = new nfs_fh4[1];
        layout.nfl_fh_list[0] = new nfs_fh4();
        layout.nfl_fh_list[0].value = context.currentInode().toFullString().getBytes();
        layout.nfl_first_stripe_index = new uint32_t(0);

        layout.nfl_util = new nfl_util4(new uint32_t( NFSv4Defaults.NFS4_STRIPE_SIZE &
                nfs4_prot.NFL4_UFLG_STRIPE_UNIT_SIZE_MASK));
        layout.nfl_util = new nfl_util4(new uint32_t(layout.nfl_util.value.value |
                nfs4_prot.NFL4_UFLG_DENSE));

        //where the striping pattern starts
        layout.nfl_pattern_offset = new offset4(new uint64_t(0));

        XdrEncodingStream xdr = new XdrBuffer(512);
        xdr.beginEncoding();
        layout.xdrEncode(xdr);
        xdr.endEncoding();

        ByteBuffer body = xdr.body();
        byte[] retBytes = new byte[body.limit()];
        body.get(retBytes);

        if (_args.oplayoutget.loga_maxcount.value.value < retBytes.length){
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_TOOSMALL, "maximum layout size");
        }


        /*
         * if file size > 2
         *
         * return two segments
         */

        layout4[] layoutArray;

        //if( context.currentInode().statCache().getSize() > 2 ) {  // disabled for now. wait for Austing-08 Bakeathon
        if( false ) {  // disabled for now. wait for Austing-08 Bakeathon
            _log.log(Level.FINER, "Using mutlisegment layout");
            layoutArray = new layout4[2];
            long halfLen = context.currentInode().statCache().getSize() / 2;
            if ( halfLen == 0) halfLen = 1024;
            /*
             * first half
             */
            layoutArray[0] = new layout4();
            layoutArray[0].lo_length = new length4(new uint64_t(halfLen));
            layoutArray[0].lo_offset = new offset4(new uint64_t(0));
            layoutArray[0].lo_iomode = _args.oplayoutget.loga_iomode;
            layoutArray[0].lo_content = new layout_content4();
            layoutArray[0].lo_content.loc_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
            layoutArray[0].lo_content.loc_body = retBytes;

            /*
             * second half
             */
            layoutArray[1] = new layout4();
            layoutArray[1].lo_length = new length4(new uint64_t(-1)); // -1 is special value, which means entire file
            layoutArray[1].lo_offset = new offset4( layoutArray[0].lo_length.value );
            layoutArray[1].lo_iomode = _args.oplayoutget.loga_iomode;
            layoutArray[1].lo_content = new layout_content4();
            layoutArray[1].lo_content.loc_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
            layoutArray[1].lo_content.loc_body = retBytes;


        }else{
            layoutArray = new layout4[1];
            layoutArray[0] = new layout4();
            layoutArray[0].lo_length = new length4(new uint64_t(-1)); // -1 is special value, which means entire file
            layoutArray[0].lo_offset = new offset4(new uint64_t(0));
            layoutArray[0].lo_iomode = _args.oplayoutget.loga_iomode;
            layoutArray[0].lo_content = new layout_content4();
            layoutArray[0].lo_content.loc_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
            layoutArray[0].lo_content.loc_body = retBytes;
        }

        res.logr_resok4.logr_layout = layoutArray;
        res.logr_resok4.logr_stateid = _args.oplayoutget.loga_stateid;
        res.logr_resok4.logr_return_on_close = true;

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
