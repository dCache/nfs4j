package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;

/**
 *
 * NFSv4 operation abstraction
 *
 */
public abstract class AbstractNFSv4Operation {

    protected final nfs_resop4 _result = new nfs_resop4();
    protected final nfs_argop4 _args;

    public AbstractNFSv4Operation(nfs_argop4 args, int opCode) {
        _result.resop = opCode;
        _args = args;
    }

    /**
     * Process current operation.
     * @return <code>true</code> if next operation may continue.
     */
    public abstract boolean process(CompoundContext context);
}
