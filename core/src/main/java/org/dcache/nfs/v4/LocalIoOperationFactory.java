/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.v4.ds.DSOperationCOMMIT;
import org.dcache.nfs.v4.ds.DSOperationREAD;
import org.dcache.nfs.v4.ds.DSOperationWRITE;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.vfs.FsCache;

/**
 * NFS operation factory which uses Proxy IO adapter for read requests
 */
public class LocalIoOperationFactory extends MDSOperationFactory {

    private final FsCache _fs;

    public LocalIoOperationFactory(FsCache fs) {
	_fs = fs;
    }

    @Override
    public AbstractNFSv4Operation getOperation(nfs_argop4 op) {
	switch (op.argop) {
	    case nfs_opnum4.OP_READ:
		return new DSOperationREAD(op, _fs);
	    case nfs_opnum4.OP_COMMIT:
		return new DSOperationCOMMIT(op, _fs);
	    case nfs_opnum4.OP_WRITE:
		return new DSOperationWRITE(op, _fs);
	    default:
		return super.getOperation(op);
	}
    }

}
