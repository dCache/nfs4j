/*
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.CodeSource;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcache.chimera.nfs.v4.NFSv4Defaults.NFS4_IMPLEMENTATION_DOMAIN;
import static org.dcache.chimera.nfs.v4.NFSv4Defaults.NFS4_IMPLEMENTATION_ID;
import org.dcache.chimera.nfs.v4.xdr.*;

public class OperationEXCHANGE_ID extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationEXCHANGE_ID.class);
    private final int _flag;
    private static final int EXCHGID4_FLAG_MASK = (nfs4_prot.EXCHGID4_FLAG_USE_PNFS_DS
            | nfs4_prot.EXCHGID4_FLAG_USE_NON_PNFS
            | nfs4_prot.EXCHGID4_FLAG_USE_PNFS_MDS
            | nfs4_prot.EXCHGID4_FLAG_SUPP_MOVED_MIGR
            | nfs4_prot.EXCHGID4_FLAG_SUPP_MOVED_REFER
            | nfs4_prot.EXCHGID4_FLAG_MASK_PNFS
            | nfs4_prot.EXCHGID4_FLAG_UPD_CONFIRMED_REC_A
            | nfs4_prot.EXCHGID4_FLAG_CONFIRMED_R
            | nfs4_prot.EXCHGID4_FLAG_BIND_PRINC_STATEID
            | nfs4_prot.EXCHGID4_FLAG_UPD_CONFIRMED_REC_A
            | nfs4_prot.EXCHGID4_FLAG_CONFIRMED_R);

    /**
     * compile time
     */
    private static String COMPILTE_TIME = "<UNKNOWN>";

    static {
        /*
         * get 'Build-Time' attribute from jar file manifest ( if available )
         */
        try {

            ProtectionDomain pd = OperationEXCHANGE_ID.class.getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            URL u = cs.getLocation();

            InputStream is = u.openStream();
            JarInputStream jis = new JarInputStream(is);
            Manifest m = jis.getManifest();

            if (m != null) {
                Attributes as = m.getMainAttributes();
                String buildTime = as.getValue("Build-Time");
                if( buildTime != null ) {
                    COMPILTE_TIME = buildTime;
                }
            }

        }catch(IOException ioe) {
            // bad luck
        }
    }

    public OperationEXCHANGE_ID(nfs_argop4 args, int flag) {
        super(args, nfs_opnum4.OP_EXCHANGE_ID);
        _flag = flag;
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws ChimeraNFSException {

        final EXCHANGE_ID4res res = result.opexchange_id;

        /*
         * Servers MUST accept a zero length eia_client_impl_id array so this
         * information is not always present!!!
         *
         * for( nfs_impl_id4 impelemtation :
         * _args.opexchange_id.eia_client_impl_id ) { _log.info("EXCHANGE_ID4: "
         * + new String(impelemtation.nii_name.value.value) );
            }
         */

        final byte[] clientOwner = _args.opexchange_id.eia_clientowner.co_ownerid;

        /*
         * check the state
         */

        if (_args.opexchange_id.eia_state_protect.spa_how != state_protect_how4.SP4_NONE && _args.opexchange_id.eia_state_protect.spa_how != state_protect_how4.SP4_MACH_CRED && _args.opexchange_id.eia_state_protect.spa_how != state_protect_how4.SP4_SSV) {
            _log.debug("EXCHANGE_ID4: state protection : {}", _args.opexchange_id.eia_state_protect.spa_how);
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "invalid state protection");
        }


        if (_args.opexchange_id.eia_flags.value != 0 && (_args.opexchange_id.eia_flags.value | EXCHGID4_FLAG_MASK) != EXCHGID4_FLAG_MASK) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "invalid flag");
        }

        /*
         * spec. requires <1>
         */
        if (_args.opexchange_id.eia_client_impl_id.length > 1) {
            throw new ChimeraNFSException(nfsstat.NFSERR_BADXDR, "invalid array size of client implementaion");
        }

        /*
         * The EXCHGID4_FLAG_CONFIRMED_R bit can only be set in eir_flags; it is
         * always off in eia_flags.
         */
        if (_args.opexchange_id.eia_flags.value != 0 && ((_args.opexchange_id.eia_flags.value & nfs4_prot.EXCHGID4_FLAG_CONFIRMED_R) == nfs4_prot.EXCHGID4_FLAG_CONFIRMED_R)) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "Client used server-only flag");
        }


        //Check if there is another ssv use -> TODO: Implement SSV
        if (_args.opexchange_id.eia_state_protect.spa_how != state_protect_how4.SP4_NONE) {
            _log.debug("Tried the wrong security Option! {}:", _args.opexchange_id.eia_state_protect.spa_how);
            throw new ChimeraNFSException(nfsstat.NFSERR_ACCESS, "SSV other than SP4NONE to use");
        }

        NFS4Client client = context.getStateHandler().clientByOwner(clientOwner);
        final Principal principal = context.getPrincipal();
        final verifier4 verifier = _args.opexchange_id.eia_clientowner.co_verifier;

        final boolean update = (_args.opexchange_id.eia_flags.value & nfs4_prot.EXCHGID4_FLAG_UPD_CONFIRMED_REC_A) != 0;

        final InetSocketAddress remoteSocketAddress = context.getRpcCall().getTransport().getRemoteSocketAddress();
        final InetSocketAddress localSocketAddress = context.getRpcCall().getTransport().getLocalSocketAddress();
        final NFSv4StateHandler stateHandler = context.getStateHandler();

        if (update) {
            if (client == null || !client.isConfirmed()) {
                _log.debug("Update of no existing/confirmed Record (case 7)");
                throw new ChimeraNFSException(nfsstat.NFSERR_NOENT, "no such client");
            }

            if (!client.verifierEquals(verifier)) {
                _log.debug("case 8: Update but Wrong Verifier");
                throw new ChimeraNFSException(nfsstat.NFSERR_NOT_SAME, "Update but Wrong Verifier");
            }

            if (!principal.equals(client.principal())) {
                _log.debug("case 9: Update but Wrong Principal");
                throw new ChimeraNFSException(nfsstat.NFSERR_PERM, "Principal Mismatch");
            }

            _log.debug("Case 6: Update");
            client.refreshLeaseTime();
        } else {
            if (client == null) {
                // create a new client: case 1
                _log.debug("Case 1: New Owner ID");
                client = stateHandler.createClient(
                        remoteSocketAddress, localSocketAddress,
                        clientOwner, _args.opexchange_id.eia_clientowner.co_verifier, principal);

            } else {

                if (client.isConfirmed()) {
                    if (client.verifierEquals(verifier) && principal.equals(client.principal())) {
                        _log.debug("Case 2: Non-Update on Existing Client ID");
                        client.refreshLeaseTime();
                    } else if (principal.equals(client.principal())) {
                        _log.debug("case 5: Client Restart");
                        stateHandler.removeClient(client);
                        client = stateHandler.createClient(
                                remoteSocketAddress, localSocketAddress,
                                clientOwner, _args.opexchange_id.eia_clientowner.co_verifier, principal);
                    } else {
                        _log.debug("Case 3b: Client Collision");
                        if ((!client.hasState()) || !client.isLeaseValid()) {
                            stateHandler.removeClient(client);
                            client = stateHandler.createClient(
                                    remoteSocketAddress, localSocketAddress,
                                    clientOwner, _args.opexchange_id.eia_clientowner.co_verifier, principal);
                        } else {
                            throw new ChimeraNFSException(nfsstat.NFSERR_CLID_INUSE, "Principal Missmatch");
                        }
                    }
                } else {
                    _log.debug("case 4: Replacement of Unconfirmed Record");
                    stateHandler.removeClient(client);
                    client = stateHandler.createClient(
                            remoteSocketAddress, localSocketAddress,
                            _args.opexchange_id.eia_clientowner.co_ownerid,
                            _args.opexchange_id.eia_clientowner.co_verifier,
                            principal);
                }
            }
        }

        client.updateLeaseTime();

        res.eir_resok4 = new EXCHANGE_ID4resok();
        res.eir_resok4.eir_clientid = new clientid4(new uint64_t(client.getId()));
        res.eir_resok4.eir_sequenceid = new sequenceid4(new uint32_t(client.currentSeqID()));
        res.eir_resok4.eir_flags = new uint32_t(_flag);

        ServerIdProvider serverIdProvider = context.getServerIdProvider();
        res.eir_resok4.eir_server_owner = serverIdProvider.getOwner();
        res.eir_resok4.eir_server_scope = serverIdProvider.getScope();

        res.eir_resok4.eir_server_impl_id = new nfs_impl_id4[1];
        res.eir_resok4.eir_server_impl_id[0] = new nfs_impl_id4();
        res.eir_resok4.eir_server_impl_id[0].nii_domain = new utf8str_cis(NFS4_IMPLEMENTATION_DOMAIN);
        res.eir_resok4.eir_server_impl_id[0].nii_name = new utf8str_cs(NFS4_IMPLEMENTATION_ID + " build-time " + COMPILTE_TIME);
        nfstime4 releaseDate = new nfstime4();
        releaseDate.nseconds = new uint32_t(0);
        releaseDate.seconds = new int64_t(System.currentTimeMillis() / 1000);
        res.eir_resok4.eir_server_impl_id[0].nii_date = releaseDate;

        res.eir_resok4.eir_state_protect = new state_protect4_r();
        res.eir_resok4.eir_state_protect.spr_how = state_protect_how4.SP4_NONE;

        if (client.isConfirmed()) {
            res.eir_resok4.eir_flags = new uint32_t(res.eir_resok4.eir_flags.value | nfs4_prot.EXCHGID4_FLAG_CONFIRMED_R);
        }
    }
}
