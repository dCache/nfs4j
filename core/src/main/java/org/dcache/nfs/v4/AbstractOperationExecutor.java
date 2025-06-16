/*
 * Copyright (c) 2019 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
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
package org.dcache.nfs.v4;

import java.io.IOException;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadSessionException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.status.ExpiredException;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.OpIllegalException;
import org.dcache.nfs.status.ResourceException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.status.StaleClientidException;
import org.dcache.nfs.status.StaleStateidException;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of {@code RequestExecutor} that provides a generic framework for nfs request execution.
 */
public abstract class AbstractOperationExecutor implements OperationExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOperationExecutor.class);

    @Override
    public nfs_resop4 execute(CompoundContext context, nfs_argop4 args) throws IOException, OncRpcException {

        AbstractNFSv4Operation op = getOperation(args);
        nfs_resop4 opResult = nfs_resop4.resopFor(args.argop);

        try {
            op.process(context, opResult);
        } catch (NfsIoException | ResourceException | ServerFaultException e) {
            LOGGER.error("NFS server fault: op: {} : {}", nfs_opnum4.toString(args.argop), e.getMessage());
            opResult.setStatus(e.getStatus());
        } catch (BadXdrException | OpIllegalException | InvalException e) {
            LOGGER.warn("Faulty NFS client: op: {} : {}", nfs_opnum4.toString(args.argop), e.getMessage());
            opResult.setStatus(e.getStatus());
        } catch (BadStateidException | StaleStateidException | ExpiredException
                | BadSessionException | StaleClientidException e) {
            LOGGER.info("Lost client state: op: {} : {}", nfs_opnum4.toString(args.argop), e.getMessage());
            opResult.setStatus(e.getStatus());
        } catch (ChimeraNFSException e) {
            opResult.setStatus(e.getStatus());
        } catch (OncRpcException e) {
            opResult.setStatus(nfsstat.NFSERR_BADXDR);
            LOGGER.warn("Bad xdr: {}: ", e.getMessage());
        }
        return opResult;
    }

    /**
     * Get an implementation of {@link AbstractNFSv4Operation} that should be executed for given nfs operation.
     *
     * @param op nfs operation arguments.
     * @return an implementation of {@link AbstractNFSv4Operation} that should be executed for given nfs operation.
     */
    abstract protected AbstractNFSv4Operation getOperation(nfs_argop4 op);
}
