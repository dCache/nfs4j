package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.open_delegation_type4;
import org.dcache.chimera.nfs.v4.xdr.change_info4;
import org.dcache.chimera.nfs.v4.xdr.bitmap4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.changeid4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.opentype4;
import org.dcache.chimera.nfs.v4.xdr.open_claim_type4;
import org.dcache.chimera.nfs.v4.xdr.fattr4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.open_delegation4;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.createmode4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.OPEN4args;
import org.dcache.chimera.nfs.v4.xdr.OPEN4resok;
import org.dcache.chimera.nfs.v4.xdr.OPEN4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.FileNotFoundHimeraFsException;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.Stat;
import org.dcache.chimera.posix.UnixAcl;

public class OperationOPEN extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(OperationOPEN.class.getName());


    OperationOPEN(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_OPEN);
    }

    @Override
    public boolean process(CompoundContext context) {
        OPEN4res res = null;

        try {


            Long clientid = Long.valueOf(_args.opopen.owner.value.clientid.value.value);
            NFS4Client client = null;

            if(context.getSession() == null ) {
                client = NFSv4StateHandler.getInstace().getClientByID(clientid);

                if (client == null || !client.isConfirmed() ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_STALE_CLIENTID, "bad client id.");
                }

                client.updateLeaseTime(NFSv4Defaults.NFS4_LEASE_TIME);
                _log.log(Level.FINEST, "open request form clientid: {0}, owner: {1}",
                        new Object[] {client, new String(_args.opopen.owner.value.owner)}
                );
            }else{
                client = context.getSession().getClient();
            }

            if (_args.opopen.claim.file.value.value.value.length > NFSv4Defaults.NFS4_MAXFILENAME) {
                res = new OPEN4res();
                res.status = nfsstat4.NFS4ERR_NAMETOOLONG;
            } else {

                if (_args.opopen.openhow.opentype == opentype4.OPEN4_CREATE) {
                    res = OPEN4_CREATE(context, _args.opopen);
                } else {
                    res = OPEN4_NOCREATE(context, _args.opopen);
                }

            }

            NFS4State nfs4state = null;
            /*
             * if it's not session-based  request, then client have to confirm
             */
            if(context.getSession() == null ){
                res.resok4.rflags = new uint32_t( nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX | nfs4_prot.OPEN4_RESULT_CONFIRM);
                nfs4state = new NFS4State( _args.opopen.seqid.value.value);

                res.resok4.stateid = new stateid4();
                res.resok4.stateid.seqid = new uint32_t(nfs4state.seqid());
                res.resok4.stateid.other = nfs4state.other();
            }else {

                res.resok4.rflags = new uint32_t(nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX);
                nfs4state = new NFS4State(context.getSession().getClient().currentSeqID());

                res.resok4.stateid = new stateid4();
                res.resok4.stateid.seqid = new uint32_t(nfs4state.seqid());
                res.resok4.stateid.other = nfs4state.other();
                context.getSession().getClient().nextSeqID();

            }

            client.addState(nfs4state);
            String stateID = new String( nfs4state.other() );
            NFSv4StateHandler.getInstace().addClinetByStateID(stateID, clientid);
            _log.log(Level.FINEST, "New stateID: {0} seqid: {1}",
                    new Object[] {stateID, nfs4state.seqid()}
            );

        } catch (ChimeraNFSException he) {
            _log.log(Level.FINE, "OPEN: ", he.getMessage());
            res = new OPEN4res();
            res.status = he.getStatus();
        }catch(FileNotFoundHimeraFsException fnf) {
            _log.log(Level.FINE, "OPEN: "+fnf.getMessage());
            res = new OPEN4res();
            res.status = nfsstat4.NFS4ERR_NOENT;
        } catch (ChimeraFsException hfe) {
            _log.log(Level.WARNING, "OPEN:",hfe);
            res = new OPEN4res();
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        } catch (Exception e) {
            _log.log(Level.SEVERE, "OPEN:",e);
            res = new OPEN4res();
            res.status = nfsstat4.NFS4ERR_SERVERFAULT;
        }


           _result.opopen = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;

    }


    private OPEN4res OPEN4_CREATE(CompoundContext context, OPEN4args args)
    throws Exception {

    OPEN4res res = new OPEN4res();
    res.status = nfsstat4.NFS4ERR_MOVED;

        boolean exclusive = args.openhow.how.mode == createmode4.EXCLUSIVE4;
        bitmap4 bitmap = null;
        res.resok4 = new OPEN4resok();
        int claim = args.claim.claim;

        FsInode inode = null;

        switch(claim) {

            case open_claim_type4.CLAIM_NULL:
                // regular open

                boolean exist = true;

                String name = NameFilter.convert(args.claim.file.value.value.value);

               /* if (args.seqid.value.value != 0){
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "Using non-zero seqid in OPEN");
                }*/

                /*if (args.owner.value.clientid.value.value != 0 ){
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "Using non-zero clientid in open_owner");
                }*/

                if( name.length() == 0 ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "zero-length name");
                }

                _log.log(Level.FINEST, "regular open for : {0}", name);

                if( !context.currentInode().isDirectory() ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "not a directory");
                }

                try {
                    inode = context.currentInode().inodeOf(name);

                }catch(ChimeraFsException he) {
                    exist = false;
                }


                if( exclusive && exist ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_EXIST, "file already exist");
                }

                _log.log(Level.FINEST, "Does the file already exists: {0}", exist);
                // check permissions
                if( exist ) {
                    _log.finest("Check permission");
                    // check file permissions
                    Stat fileStat = inode.statCache();
                    _log.log(Level.FINEST, "UID  : {0}", fileStat.getUid());
                    _log.log(Level.FINEST, "GID  : {0}", fileStat.getGid());
                    _log.log(Level.FINEST, "Mode : 0{0}", Integer.toOctalString(fileStat.getMode() & 0777) );
                    UnixAcl fileAcl = new UnixAcl(fileStat.getUid(), fileStat.getGid(),fileStat.getMode() & 0777 );
                    if ( ! _permissionHandler.isAllowed(fileAcl, context.getUser(), AclHandler.ACL_WRITE) ) {
                        throw new ChimeraNFSException( nfsstat4.NFS4ERR_ACCESS, "Permission denied."  );
                    }
                    res.resok4.attrset = new bitmap4();
                    res.resok4.attrset.value = new uint32_t[2];
                    res.resok4.attrset.value[0] = new uint32_t(0);
                    res.resok4.attrset.value[1] = new uint32_t(0);


                }else{
                    // check parent permissions
                    Stat parentStat = context.currentInode().statCache();
                    UnixAcl parentAcl = new UnixAcl(parentStat.getUid(), parentStat.getGid(),parentStat.getMode() & 0777 );
                    if ( ! _permissionHandler.isAllowed(parentAcl, context.getUser(), AclHandler.ACL_INSERT) ) {
                        throw new ChimeraNFSException( nfsstat4.NFS4ERR_ACCESS, "Permission denied."  );
                    }
                    res.resok4.attrset = new bitmap4();
                    res.resok4.attrset.value = new uint32_t[2];
                    res.resok4.attrset.value[0] = new uint32_t(0);
                    res.resok4.attrset.value[1] = new uint32_t(0);

                }

                _log.log(Level.FINEST, "Does the file already exists: {0}", exist);
                if( ! exist ) {
                    inode = context.currentInode().create(name, context.getUser().getUID(), context.getUser().getGID(), 0600);

                    if( ! exclusive ){
                        fattr4 createAttr = args.openhow.how.createattrs;
                        bitmap = OperationSETATTR.setAttributes(createAttr, inode);

                        res.resok4.attrset = new bitmap4();
                        res.resok4.attrset.value = bitmap.value;

                    }

                }else{
                    OperationSETATTR.setAttributes(args.openhow.how.createattrs, inode);
                }

                res.resok4.cinfo = new change_info4();
                res.resok4.cinfo.atomic = true;
                res.resok4.cinfo.before = new changeid4( new uint64_t(context.currentInode().statCache().getMTime()));
                res.resok4.cinfo.after = new changeid4( new uint64_t( System.currentTimeMillis()) );

                res.resok4.delegation = new open_delegation4();
                res.resok4.delegation.delegation_type = open_delegation_type4.OPEN_DELEGATE_NONE;

                res.status = nfsstat4.NFS4_OK;

                context.currentInode(inode);

                break;
            case open_claim_type4.CLAIM_PREVIOUS:
                _log.log(Level.FINEST, "open by Inode for : {0}", context.currentInode().toFullString() );
                break;
            case open_claim_type4.CLAIM_DELEGATE_CUR:
                break;
            case open_claim_type4.CLAIM_DELEGATE_PREV:
                break;
        }


    return res;
}

private OPEN4res OPEN4_NOCREATE(CompoundContext context, OPEN4args args)
    throws Exception {

    OPEN4res res = new OPEN4res();


        int claim = args.claim.claim;
        FsInode inode = null;

        switch(claim) {

            case open_claim_type4.CLAIM_NULL:
                // regular open

                String name = new String (args.claim.file.value.value.value);

                /*if (args.seqid.value.value != 0){
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "Using non-zero seqid in OPEN");
                }

                if (args.owner.value.clientid.value.value != 0 ){
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "Using non-zero clientid in open_owner");
                }*/

                if( name.length() == 0 ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_INVAL, "zero-length name");
                }

                _log.log(Level.FINEST, "regular open for : {0}", name);

                if( !context.currentInode().isDirectory() ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOTDIR, "not a directory");
                }

                inode = context.currentInode().inodeOf(name);

                if( inode.isDirectory() ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_ISDIR, "path is a directory");
                }

                if( inode.isLink() ) {
                    throw new ChimeraNFSException(nfsstat4.NFS4ERR_SYMLINK, "path is a symlink");
                }

                res.resok4 = new OPEN4resok();
                res.resok4.attrset = new bitmap4();
                res.resok4.attrset.value = new uint32_t[2];
                res.resok4.attrset.value[0] = new uint32_t(0);
                res.resok4.attrset.value[1] = new uint32_t(0);

                res.resok4.cinfo = new change_info4();
                res.resok4.cinfo.atomic = true;
                res.resok4.cinfo.before = new changeid4( new uint64_t(context.currentInode().statCache().getMTime()));
                res.resok4.cinfo.after = new changeid4( new uint64_t( System.currentTimeMillis()) );

                res.resok4.delegation = new open_delegation4();
                res.resok4.delegation.delegation_type = open_delegation_type4.OPEN_DELEGATE_NONE;
                res.resok4.rflags = new uint32_t( nfs4_prot.OPEN4_RESULT_LOCKTYPE_POSIX );

                context.currentInode(inode);

                break;
            case open_claim_type4.CLAIM_PREVIOUS:
                _log.log(Level.FINEST, "open by Inode for : {0}", context.currentInode().toFullString() );
                break;
            case open_claim_type4.CLAIM_DELEGATE_CUR:
                break;
            case open_claim_type4.CLAIM_DELEGATE_PREV:
                break;
        }

    return res;
}


}
