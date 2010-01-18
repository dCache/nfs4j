/*
 * $Id:NFSProtocol_4.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4.mover;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4args;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4res;
import org.dcache.chimera.nfs.v4.CompoundContext;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.NFSv4Call;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcDispatchable;
import org.dcache.xdr.XdrVoid;

/**
 *
 * dCache pool simulator
 *
 */

public class NFSProtocol_4 implements RpcDispatchable {

	private static final Logger _log = Logger.getLogger(NFSProtocol_4.class.getName());

	private final FileSystemProvider _fs;

	public NFSProtocol_4(FileSystemProvider fs,  File poolRoot) {
       _fs = fs;
	}

	public void dispatchOncRpcCall(RpcCall call) throws OncRpcException, IOException {

        int version = call.getProgramVersion();
        int procedure = call.getProcedure();

		if (version == 4) {
			switch (procedure) {
			case 0: {
				call.retrieveCall(XdrVoid.XDR_VOID);
				NFSPROC4_NULL_4(call);
				call.reply(XdrVoid.XDR_VOID);
				break;
			}
			case 1: {
				COMPOUND4args args$ = new COMPOUND4args();
				call.retrieveCall(args$);
				COMPOUND4res result$ = NFSPROC4_COMPOUND_4(call, args$);
				call.reply(result$);
				break;
			}
			default:
				call.failProcedureUnavailable();
			}
		} else {
			call.failProgramUnavailable();
		}

	}

	public void NFSPROC4_NULL_4(RpcCall call$) {
		_log.log(Level.FINE, "MOVER: PING from client: {0}",
            call$.getTransport().getRemoteSocketAddress() );
	}

	public COMPOUND4res NFSPROC4_COMPOUND_4(RpcCall call$, COMPOUND4args arg1) {

		COMPOUND4res res = new COMPOUND4res();

    	_log.log(Level.FINE, "MOVER: NFS COMPOUND client: {0}, tag: {1}",
                new Object[] {
                    call$.getTransport().getRemoteSocketAddress(),
                    new String(arg1.tag.value.value)
                }
        );




        try {

                _log.log(Level.FINE, "NFS COMPOUND client: {0}, tag: {1}",
                        new Object[]{call$.getTransport().getRemoteSocketAddress(),
                            new String(arg1.tag.value.value)}
                );

                	List<nfs_resop4> v = new LinkedList<nfs_resop4>();
	        	if( arg1.minorversion.value > 1 ) {
	        	    res.status = nfsstat4.NFS4ERR_MINOR_VERS_MISMATCH;
                    _log.log(Level.FINE,"      : NFS4ERR_MINOR_VERS_MISMATCH");
                    }else{


                CompoundContext context = new CompoundContext(v, arg1.minorversion.value,
                        _fs, call$, null);


                for (nfs_argop4 op : arg1.argarray) {

                    if (!DSNFSv4OperationFactory.getOperation(op).process(context)) {
                        break;
                    }
                }

                try {
                    _log.log(Level.FINE, "CURFH: {0}", context.currentInode().toFullString());
                } catch (ChimeraNFSException he) {
                    _log.fine("CURFH: NULL");
                }
                v = context.processedOperations();
            }

            res.tag = arg1.tag;

            res.resarray = v.toArray(new nfs_resop4[v.size()]);
            // result  status must be equivalent
            // to the status of the last operation that
            // was executed within the COMPOUND procedure

            res.status = res.resarray[res.resarray.length - 1].getStatus();

            _log.log(Level.FINE, "OP: {1} status: {1}",
                    new Object[]{NFSv4Call.toString(res.resarray[res.resarray.length - 1].resop), res.status});
    		}catch(Exception e) {
    			_log.log(Level.SEVERE,"Unhandled exception:", e);
    		}

		return res;

    }

}
