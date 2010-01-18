package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.UnixPermissionHandler;


/**
 *
 * NFSv4 operation abstraction
 *
 */
public abstract class AbstractNFSv4Operation {


	protected final nfs_resop4 _result = new nfs_resop4();
        protected  final nfs_argop4 _args;
        protected AclHandler _permissionHandler = UnixPermissionHandler.getInstance();

	public AbstractNFSv4Operation(nfs_argop4 args, int opCode) {
		_result.resop = opCode;
                _args = args;
	}

        /**
         * Process current opration.
         * @return <code>true</code> if next operration may continue.
         */
	public abstract boolean process(CompoundContext context);

}
