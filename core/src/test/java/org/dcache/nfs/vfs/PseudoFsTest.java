/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.vfs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.stream.Stream;
import org.dcache.auth.Subjects;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.PermException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthType;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.gss.RpcAuthGss;
import org.dcache.xdr.gss.RpcGssService;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

import org.junit.Before;

/**
 *
 * @author tigran
 */
public class PseudoFsTest {

    private final static InetSocketAddress localAddress = new InetSocketAddress(0);
    private VirtualFileSystem mockedFs;
    private ExportFile mockedExportFile;
    private FsExport mockedExport;
    private XdrTransport mockedTransport;
    private RpcCall mockedRpc;
    private RpcAuth mockedAuth;
    private PseudoFs pseudoFs;
    private Inode inode;

    @Before
    public void setUp() throws IOException {

        mockedFs = mock(VirtualFileSystem.class);
        mockedExportFile = mock(ExportFile.class);
        mockedTransport = mock(XdrTransport.class);
        mockedRpc = mock(RpcCall.class);
        mockedAuth = mock(RpcAuth.class);
        mockedExport = mock(FsExport.class);
        given(mockedExport.getAnonUid()).willReturn(FsExport.DEFAULT_ANON_UID);
        given(mockedExport.getAnonGid()).willReturn(FsExport.DEFAULT_ANON_GID);

        // prepare file system
        inode = mock(Inode.class);
        given(inode.isPesudoInode()).willReturn(false);
        given(inode.exportIndex()).willReturn(1);
        given(inode.handleVersion()).willReturn(1);

        Stat stat = new Stat();
        stat.setMode(0700 | Stat.S_IFREG);
        stat.setUid(1);
        stat.setGid(1);

        given(mockedFs.getattr(inode)).willReturn(stat, stat);
    }

    @Test(expected = AccessException.class)
    public void testRootSquash() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(false);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.NONE);

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(mockedExport);
        given(mockedExportFile.exportsFor(localAddress.getAddress())).willReturn(Stream.of(mockedExport));


        given(mockedFs.create(inode, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 644))
                .willReturn( mock(Inode.class));

        pseudoFs = new PseudoFs(mockedFs, mockedRpc, mockedExportFile);
        pseudoFs.create(inode, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 644);
    }

    @Test
    public void testNoRootSquash() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(true);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.NONE);

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(mockedExport);
        given(mockedExportFile.exportsFor(localAddress.getAddress())).willReturn(Stream.of(mockedExport));

        given(mockedFs.create(inode, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 644))
                .willReturn(mock(Inode.class));

        pseudoFs = new PseudoFs(mockedFs, mockedRpc, mockedExportFile);
        pseudoFs.create(inode, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 644);
    }

    @Test(expected = AccessException.class)
    public void testAllSquash() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(true);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.hasAllSquash()).willReturn(true);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.NONE);

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(mockedExport);
        given(mockedExportFile.exportsFor(localAddress.getAddress())).willReturn(Stream.of(mockedExport));

        given(mockedFs.create(inode, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 644))
                .willReturn(mock(Inode.class));

        pseudoFs = new PseudoFs(mockedFs, mockedRpc, mockedExportFile);
        pseudoFs.create(inode, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 644);
    }

    @Test(expected = PermException.class)
    public void testAuthFlavorTooWeak() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(true);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.KRB5);

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(mockedExport);
        given(mockedExportFile.exportsFor(localAddress.getAddress())).willReturn(Stream.of(mockedExport));

        pseudoFs = new PseudoFs(mockedFs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(inode);
    }

    @Test
    public void testAuthFlavorMatch() throws IOException {

        RpcAuthGss mockedAuthGss = mock(RpcAuthGss.class);

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuthGss.getSubject()).willReturn(Subjects.ROOT);
        given(mockedAuthGss.type()).willReturn(RpcAuthType.RPCGSS_SEC);
        given(mockedAuthGss.getService()).willReturn(RpcGssService.RPC_GSS_SVC_NONE);

        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuthGss);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(true);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.KRB5);

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(mockedExport);
        given(mockedExportFile.exportsFor(localAddress.getAddress())).willReturn(Stream.of(mockedExport));

        pseudoFs = new PseudoFs(mockedFs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(inode);
    }

    @Test
    public void testAuthFlavorStrong() throws IOException {

        RpcAuthGss mockedAuthGss = mock(RpcAuthGss.class);

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuthGss.getSubject()).willReturn(Subjects.ROOT);
        given(mockedAuthGss.type()).willReturn(RpcAuthType.RPCGSS_SEC);
        given(mockedAuthGss.getService()).willReturn(RpcGssService.RPC_GSS_SVC_INTEGRITY);

        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuthGss);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(true);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.KRB5);

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(mockedExport);
        given(mockedExportFile.exportsFor(localAddress.getAddress())).willReturn(Stream.of(mockedExport));

        pseudoFs = new PseudoFs(mockedFs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(inode);
    }
}
