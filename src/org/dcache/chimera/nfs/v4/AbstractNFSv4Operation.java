package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.*;
import org.dcache.xdr.RpcCall;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.UnixPermissionHandler;



/**
 *
 * NFSv4 operation abstraction
 *
 */
public abstract class AbstractNFSv4Operation {


	protected final nfs_resop4 _result = new nfs_resop4();
	protected final FileSystemProvider _fs;
	protected final RpcCall _callInfo;
	protected final CompoundArgs _fh;
	protected final nfs_argop4 _args;
	protected final org.dcache.chimera.posix.UnixUser _user;
	protected final ExportFile _exportFile;

	protected AclHandler _permissionHandler = UnixPermissionHandler.getInstance();

	public AbstractNFSv4Operation(FileSystemProvider fs, ExportFile exportFile, RpcCall call$, CompoundArgs fh, nfs_argop4 args, int opCode) {
		_result.resop = opCode;
		_fs = fs;
		_callInfo = call$;
		_fh = fh;
		_args = args;
		_exportFile = exportFile;
		_user = HimeraNFS4Utils.remoteUser(_callInfo, _exportFile);
	}


	public abstract NFSv4OperationResult process();

}
