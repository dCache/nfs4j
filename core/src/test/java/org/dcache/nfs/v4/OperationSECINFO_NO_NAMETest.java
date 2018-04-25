/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
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
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.xdr.SECINFO_NO_NAME4args;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.Inode;
import org.dcache.oncrpc4j.rpc.RpcAuthType;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.mock;

import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;

public class OperationSECINFO_NO_NAMETest {

    private OperationSECINFO_NO_NAME op;
    private Inode inode;

    @Before
    public void setUp() throws IOException {

        nfs_argop4 arg =  new nfs_argop4();
        arg.argop = nfs_opnum4.OP_SECINFO_NO_NAME;
        arg.opsecinfo_no_name = new SECINFO_NO_NAME4args(0);

        op  = new OperationSECINFO_NO_NAME(arg);
        inode = mock(Inode.class);

    }
    @Test
    public void testSecUnix() throws Exception {

        ExportFile exportFile = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports.sys").toURI());
        CompoundContext context = new CompoundContextBuilder()
                .withExportFile(exportFile)
                .withCall(generateRpcCall())
                .build();
        context.currentInode(inode);
        nfs_resop4 result = nfs_resop4.resopFor(nfs_opnum4.OP_SECINFO_NO_NAME);
        op.process(context, result);

        assertEquals("Sec Sys not detected", RpcAuthType.UNIX, result.opsecinfo_no_name.resok4.value[0].flavor);
    }

    @Test
    public void testSecKrb() throws Exception {

        ExportFile exportFile = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports.krb").toURI());
        CompoundContext context = new CompoundContextBuilder()
                .withExportFile(exportFile)
                .withCall(generateRpcCall())
                .build();
        context.currentInode(inode);
        nfs_resop4 result = nfs_resop4.resopFor(nfs_opnum4.OP_SECINFO_NO_NAME);
        op.process(context, result);

        assertEquals("Sec Krb5 not detected", RpcAuthType.RPCGSS_SEC, result.opsecinfo_no_name.resok4.value[0].flavor);
    }

    @Test
    public void testSecMix() throws Exception {

        ExportFile exportFile = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports.mix").toURI());
        CompoundContext context = new CompoundContextBuilder()
                .withExportFile(exportFile)
                .withCall(generateRpcCall())
                .build();
        context.currentInode(inode);
        nfs_resop4 result = nfs_resop4.resopFor(nfs_opnum4.OP_SECINFO_NO_NAME);
        op.process(context, result);

        assertEquals("Sec Krb5 not detected", RpcAuthType.RPCGSS_SEC, result.opsecinfo_no_name.resok4.value[0].flavor);
        assertEquals("Sec Sys not detected", RpcAuthType.UNIX, result.opsecinfo_no_name.resok4.value[1].flavor);
    }
}
