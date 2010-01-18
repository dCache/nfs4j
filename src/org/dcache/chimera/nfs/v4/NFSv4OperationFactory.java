package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;

public interface NFSv4OperationFactory {

    AbstractNFSv4Operation getOperation(nfs_argop4 op);

}
